import { apiUserWithRole, assertOwnedProject } from "@/lib/api";
import { prisma } from "@/lib/db";
import { fail, ok, toErrorResponse } from "@/lib/http";
import { assertCanAddKeywords, assertEngineCount } from "@/lib/limits";
import { sortEngines } from "@/lib/engines";
import { keywordCreateSchema, parseKeywordList } from "@/lib/validation";

/** 여러 줄/쉼표 입력을 받아 한 번에 등록한다. 이미 있는 키워드는 건너뛴다. */
export async function POST(request: Request) {
  try {
    const user = await apiUserWithRole("ADMIN");
    const input = keywordCreateSchema.parse(await request.json());
    await assertOwnedProject(user.orgId, input.projectId);

    const engines = sortEngines(input.engines);
    assertEngineCount(user.org, engines);

    const texts = parseKeywordList(input.text);
    if (texts.length === 0) return fail("등록할 키워드가 없습니다.", 422);

    const existing = await prisma.keyword.findMany({
      where: { projectId: input.projectId, text: { in: texts } },
      select: { text: true },
    });
    const existingSet = new Set(existing.map((k) => k.text));
    const fresh = texts.filter((t) => !existingSet.has(t));

    if (fresh.length === 0) {
      return fail("입력한 키워드가 이미 모두 등록되어 있습니다.", 409);
    }

    await assertCanAddKeywords(user.org, fresh.length);
    await prisma.keyword.createMany({
      data: fresh.map((text) => ({ projectId: input.projectId, text, engines })),
    });

    return ok({ created: fresh.length, skipped: existingSet.size }, 201);
  } catch (error) {
    return toErrorResponse(error);
  }
}
