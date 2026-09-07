import { apiUserWithRole } from "@/lib/api";
import { prisma } from "@/lib/db";
import { ok, toErrorResponse } from "@/lib/http";
import { planSchema } from "@/lib/validation";

/**
 * 요금제 변경.
 * 결제 연동(PG) 전이므로 소유자가 직접 등급을 바꾸는 형태다.
 * 실제 서비스에서는 결제 성공 웹훅에서 이 로직을 호출하도록 바꾸면 된다.
 */
export async function PATCH(request: Request) {
  try {
    const user = await apiUserWithRole("OWNER");
    const input = planSchema.parse(await request.json());

    const org = await prisma.organization.update({
      where: { id: user.orgId },
      data: { plan: input.plan, planSince: new Date() },
    });
    return ok({ plan: org.plan });
  } catch (error) {
    return toErrorResponse(error);
  }
}
