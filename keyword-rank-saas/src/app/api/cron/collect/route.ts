import { prisma } from "@/lib/db";
import { fail, ok, toErrorResponse } from "@/lib/http";
import { planOf } from "@/lib/plan";
import { loadTargets, runCollect } from "@/lib/rank";

export const runtime = "nodejs";
export const maxDuration = 300;

/**
 * 매일 1회 호출되는 자동 수집 엔드포인트.
 * Vercel Cron 또는 외부 스케줄러에서
 *   GET /api/cron/collect  (Authorization: Bearer $CRON_SECRET)
 * 형태로 호출한다.
 */
async function handle(request: Request) {
  try {
    const secret = process.env.CRON_SECRET;
    if (!secret) return fail("CRON_SECRET 이 설정되지 않았습니다.", 500);

    const header = request.headers.get("authorization") ?? "";
    const queryKey = new URL(request.url).searchParams.get("key");
    const authorized = header === `Bearer ${secret}` || queryKey === secret;
    if (!authorized) return fail("인증되지 않은 요청입니다.", 401);

    const orgs = await prisma.organization.findMany({
      select: { id: true, plan: true, name: true },
    });

    const results: { org: string; total: number; ok: number; fail: number }[] = [];
    for (const org of orgs) {
      if (!planOf(org.plan).autoDailyCollect) continue;
      const targets = await loadTargets({ orgId: org.id });
      if (targets.length === 0) continue;

      const summary = await runCollect({
        targets,
        orgId: org.id,
        triggeredBy: "cron",
      });
      results.push({
        org: org.name,
        total: summary.total,
        ok: summary.ok,
        fail: summary.fail,
      });
    }

    return ok({ organizations: results.length, results });
  } catch (error) {
    return toErrorResponse(error);
  }
}

export const GET = handle;
export const POST = handle;
