import { prisma } from "@/lib/db";
import { verifyPassword } from "@/lib/auth";
import { fail, ok, toErrorResponse } from "@/lib/http";
import { setSessionCookie, signSession } from "@/lib/session";
import { loginSchema } from "@/lib/validation";

export async function POST(request: Request) {
  try {
    const input = loginSchema.parse(await request.json());
    const user = await prisma.user.findUnique({ where: { email: input.email } });

    // 계정 존재 여부가 드러나지 않도록 동일한 메시지를 사용한다
    const invalid = () => fail("이메일 또는 비밀번호가 올바르지 않습니다.", 401);
    if (!user) return invalid();
    if (!(await verifyPassword(input.password, user.passwordHash))) return invalid();
    if (!user.active) return fail("비활성화된 계정입니다. 관리자에게 문의하세요.", 403);

    await prisma.user.update({
      where: { id: user.id },
      data: { lastLoginAt: new Date() },
    });

    await setSessionCookie(
      await signSession({ uid: user.id, orgId: user.orgId, role: user.role }),
    );
    return ok({ id: user.id });
  } catch (error) {
    return toErrorResponse(error);
  }
}
