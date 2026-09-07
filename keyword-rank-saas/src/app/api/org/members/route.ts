import { apiUserWithRole } from "@/lib/api";
import { hashPassword } from "@/lib/auth";
import { prisma } from "@/lib/db";
import { fail, ok, toErrorResponse } from "@/lib/http";
import { planOf } from "@/lib/plan";
import { memberSchema } from "@/lib/validation";

export async function POST(request: Request) {
  try {
    const user = await apiUserWithRole("ADMIN");
    const input = memberSchema.parse(await request.json());

    const seats = planOf(user.org.plan).seats;
    const count = await prisma.user.count({ where: { orgId: user.orgId } });
    if (count >= seats) {
      return fail(
        `${planOf(user.org.plan).label} 요금제는 팀원을 ${seats}명까지 등록할 수 있습니다.`,
        402,
      );
    }

    const exists = await prisma.user.findUnique({ where: { email: input.email } });
    if (exists) return fail("이미 사용 중인 이메일입니다.", 409);

    const member = await prisma.user.create({
      data: {
        orgId: user.orgId,
        email: input.email,
        name: input.name,
        role: input.role,
        passwordHash: await hashPassword(input.password),
      },
      select: { id: true, email: true, name: true, role: true },
    });
    return ok(member, 201);
  } catch (error) {
    return toErrorResponse(error);
  }
}
