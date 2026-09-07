import { apiUserWithRole, assertOwnedProject } from "@/lib/api";
import { prisma } from "@/lib/db";
import { ok, toErrorResponse } from "@/lib/http";
import { projectSchema } from "@/lib/validation";

type Ctx = { params: Promise<{ id: string }> };

export async function PATCH(request: Request, ctx: Ctx) {
  try {
    const { id } = await ctx.params;
    const user = await apiUserWithRole("ADMIN");
    await assertOwnedProject(user.orgId, id);

    const input = projectSchema.parse(await request.json());
    const project = await prisma.project.update({
      where: { id },
      data: {
        name: input.name,
        targetDomain: input.targetDomain,
        memo: input.memo || null,
      },
    });
    return ok(project);
  } catch (error) {
    return toErrorResponse(error);
  }
}

export async function DELETE(_request: Request, ctx: Ctx) {
  try {
    const { id } = await ctx.params;
    const user = await apiUserWithRole("ADMIN");
    await assertOwnedProject(user.orgId, id);
    await prisma.project.delete({ where: { id } });
    return ok({ id });
  } catch (error) {
    return toErrorResponse(error);
  }
}
