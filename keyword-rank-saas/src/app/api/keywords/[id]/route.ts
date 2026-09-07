import { apiUserWithRole, assertOwnedKeyword } from "@/lib/api";
import { prisma } from "@/lib/db";
import { ok, toErrorResponse } from "@/lib/http";
import { assertEngineCount } from "@/lib/limits";
import { sortEngines } from "@/lib/engines";
import { keywordUpdateSchema } from "@/lib/validation";

type Ctx = { params: Promise<{ id: string }> };

export async function PATCH(request: Request, ctx: Ctx) {
  try {
    const { id } = await ctx.params;
    const user = await apiUserWithRole("ADMIN");
    await assertOwnedKeyword(user.orgId, id);

    const input = keywordUpdateSchema.parse(await request.json());
    const engines = input.engines ? sortEngines(input.engines) : undefined;
    if (engines) assertEngineCount(user.org, engines);

    const keyword = await prisma.keyword.update({
      where: { id },
      data: {
        ...(engines ? { engines } : {}),
        ...(input.active === undefined ? {} : { active: input.active }),
      },
    });
    return ok(keyword);
  } catch (error) {
    return toErrorResponse(error);
  }
}

export async function DELETE(_request: Request, ctx: Ctx) {
  try {
    const { id } = await ctx.params;
    const user = await apiUserWithRole("ADMIN");
    await assertOwnedKeyword(user.orgId, id);
    await prisma.keyword.delete({ where: { id } });
    return ok({ id });
  } catch (error) {
    return toErrorResponse(error);
  }
}
