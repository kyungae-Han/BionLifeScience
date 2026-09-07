import { prisma } from "@/lib/db";
import { hashPassword, toSlug } from "@/lib/auth";
import { fail, ok, toErrorResponse } from "@/lib/http";
import { setSessionCookie, signSession } from "@/lib/session";
import { signupSchema } from "@/lib/validation";

export async function POST(request: Request) {
  try {
    if (process.env.ALLOW_SIGNUP === "false") {
      return fail("현재 신규 가입이 중단되어 있습니다.", 403);
    }

    const input = signupSchema.parse(await request.json());

    const exists = await prisma.user.findUnique({ where: { email: input.email } });
    if (exists) return fail("이미 가입된 이메일입니다.", 409);

    const passwordHash = await hashPassword(input.password);

    const user = await prisma.$transaction(async (tx) => {
      const org = await tx.organization.create({
        data: { name: input.orgName, slug: toSlug(input.orgName) },
      });
      return tx.user.create({
        data: {
          orgId: org.id,
          email: input.email,
          name: input.name,
          passwordHash,
          role: "OWNER",
          lastLoginAt: new Date(),
        },
      });
    });

    await setSessionCookie(
      await signSession({ uid: user.id, orgId: user.orgId, role: user.role }),
    );
    return ok({ id: user.id });
  } catch (error) {
    return toErrorResponse(error);
  }
}
