import Link from "next/link";

import CollectButton from "@/components/CollectButton";
import RankTable from "@/components/RankTable";
import StatTile from "@/components/StatTile";
import { requireUser } from "@/lib/auth";
import { formatDateTime } from "@/lib/date";
import { prisma } from "@/lib/db";
import { planOf } from "@/lib/plan";
import { buildKeywordRows, summarize } from "@/lib/rank";

export const dynamic = "force-dynamic";

export default async function DashboardPage() {
  const user = await requireUser();
  const plan = planOf(user.org.plan);

  const [rows, projectCount, lastRun] = await Promise.all([
    buildKeywordRows({ orgId: user.orgId }),
    prisma.project.count({ where: { orgId: user.orgId } }),
    prisma.collectRun.findFirst({
      where: { orgId: user.orgId },
      orderBy: { startedAt: "desc" },
    }),
  ]);

  const stats = summarize(rows);

  if (projectCount === 0) {
    return (
      <div className="card mx-auto max-w-xl p-10 text-center">
        <h1 className="text-xl font-bold">첫 프로젝트를 만들어 주세요</h1>
        <p className="mt-2 text-sm text-ink-2">
          순위를 추적할 사이트 도메인을 등록하면 키워드를 추가할 수 있습니다.
        </p>
        <Link href="/app/projects" className="btn btn-primary mt-6">
          프로젝트 만들기
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">대시보드</h1>
          <p className="mt-1 text-sm text-ink-2">
            {lastRun
              ? `마지막 수집 ${formatDateTime(lastRun.finishedAt ?? lastRun.startedAt)}`
              : "아직 수집 이력이 없습니다"}
            {plan.autoDailyCollect ? " · 매일 자동 수집 중" : " · 수동 수집 요금제"}
          </p>
        </div>
        <CollectButton label="전체 순위 확인" />
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatTile
          label="10위 안 노출"
          value={stats.top10}
          unit={`/ ${stats.trackedCells}`}
          hint="키워드 × 검색영역 기준"
          hero
        />
        <StatTile
          label="평균 순위"
          value={stats.avgRank ?? "-"}
          unit={stats.avgRank ? "위" : undefined}
          hint="노출된 항목만 계산"
        />
        <StatTile
          label="순위 상승"
          value={stats.up}
          unit="건"
          tone="good"
          hint="직전 수집 대비"
        />
        <StatTile
          label="순위 하락"
          value={stats.down}
          unit="건"
          tone="critical"
          hint="직전 수집 대비"
        />
      </div>

      <section className="card">
        <div className="flex items-center justify-between border-b border-line px-5 py-4">
          <h2 className="font-bold">
            전체 키워드{" "}
            <span className="num text-sm font-medium text-muted">
              {rows.length} / {plan.maxKeywords}
            </span>
          </h2>
          <Link href="/app/projects" className="btn btn-sm">
            키워드 관리
          </Link>
        </div>
        <RankTable rows={rows} showProject />
      </section>
    </div>
  );
}
