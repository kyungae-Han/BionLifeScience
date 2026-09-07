import { z } from "zod";

import { apiUser, assertOwnedKeyword, assertOwnedProject } from "@/lib/api";
import { fail, ok, toErrorResponse } from "@/lib/http";
import { assertCanManualCheck } from "@/lib/limits";
import { countChecks, loadTargets, runCollect } from "@/lib/rank";

export const runtime = "nodejs";
export const maxDuration = 60;

const bodySchema = z.object({
  projectId: z.string().optional(),
  keywordId: z.string().optional(),
});

/** "지금 확인" - 프로젝트 전체 또는 키워드 1개를 즉시 수집한다. */
export async function POST(request: Request) {
  try {
    const user = await apiUser();
    const input = bodySchema.parse(await request.json().catch(() => ({})));

    if (input.keywordId) await assertOwnedKeyword(user.orgId, input.keywordId);
    if (input.projectId) await assertOwnedProject(user.orgId, input.projectId);

    const targets = await loadTargets({
      orgId: user.orgId,
      projectId: input.projectId,
      keywordIds: input.keywordId ? [input.keywordId] : undefined,
    });
    if (targets.length === 0) return fail("수집할 키워드가 없습니다.", 422);

    await assertCanManualCheck(user.org, countChecks(targets));

    const summary = await runCollect({
      targets,
      orgId: user.orgId,
      triggeredBy: `manual:${user.id}`,
    });
    return ok(summary);
  } catch (error) {
    return toErrorResponse(error);
  }
}
