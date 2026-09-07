import { apiUserWithRole } from "@/lib/api";
import { prisma } from "@/lib/db";
import { ok, toErrorResponse } from "@/lib/http";
import { assertCanAddProject } from "@/lib/limits";
import { projectSchema } from "@/lib/validation";

export async function POST(request: Request) {
  try {
    const user = await apiUserWithRole("ADMIN");
    const input = projectSchema.parse(await request.json());
    await assertCanAddProject(user.org);

    const project = await prisma.project.create({
      data: {
        orgId: user.orgId,
        name: input.name,
        targetDomain: input.targetDomain,
        memo: input.memo || null,
      },
    });
    return ok(project, 201);
  } catch (error) {
    return toErrorResponse(error);
  }
}
