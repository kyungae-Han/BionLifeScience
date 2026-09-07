import { apiUserWithRole } from "@/lib/api";
import { prisma } from "@/lib/db";
import { fail, ok, toErrorResponse } from "@/lib/http";

type Ctx = { params: Promise<{ id: string }> };

export async function DELETE(_request: Request, ctx: Ctx) {
  try {
    const { id } = await ctx.params;
    const user = await apiUserWithRole("ADMIN");

    if (id === user.id) return fail("본인 계정은 삭제할 수 없습니다.", 400);

    const target = await prisma.user.findFirst({
      where: { id, orgId: user.orgId },
    });
    if (!target) return fail("팀원을 찾을 수 없습니다.", 404);
    if (target.role === "OWNER") return fail("소유자 계정은 삭제할 수 없습니다.", 403);

    await prisma.user.delete({ where: { id } });
    return ok({ id });
  } catch (error) {
    return toErrorResponse(error);
  }
}
