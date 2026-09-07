import Link from "next/link";
import { notFound } from "next/navigation";

import CollectButton from "@/components/CollectButton";
import KeywordForm from "@/components/KeywordForm";
import RankTable from "@/components/RankTable";
import StatTile from "@/components/StatTile";
import { requireUser } from "@/lib/auth";
import { engineStatuses } from "@/lib/collector";
import { prisma } from "@/lib/db";
import { planOf } from "@/lib/plan";
import { buildKeywordRows, summarize } from "@/lib/rank";

export const dynamic = "force-dynamic";

export default async function ProjectDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const user = await requireUser();
  const plan = planOf(user.org.plan);

  const project = await prisma.project.findFirst({
    where: { id, orgId: user.orgId },
  });
  if (!project) notFound();

  const rows = await buildKeywordRows({ orgId: user.orgId, projectId: id });
  const stats = summarize(rows);

  const liveEngines = Object.fromEntries(
    engineStatuses().map((s) => [s.engine, s.live]),
  );
  const canManage = user.role !== "MEMBER";

  return (
    <div className="space-y-6">
      <div>
        <Link href="/app/projects" className="text-sm text-muted hover:underline">
          ← 프로젝트 목록
        </Link>
        <div className="mt-3 flex flex-wrap items-end justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold">{project.name}</h1>
            <p className="mt-1 text-sm text-ink-2">{project.targetDomain}</p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            {plan.csvExport && (
              <a className="btn" href={`/api/export?projectId=${project.id}`}>
                CSV 내려받기
              </a>
            )}
            <CollectButton projectId={project.id} />
          </div>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatTile label="키워드" value={rows.length} unit="개" />
        <StatTile
          label="10위 안 노출"
          value={stats.top10}
          unit={`/ ${stats.trackedCells}`}
        />
        <StatTile
          label="평균 순위"
          value={stats.avgRank ?? "-"}
          unit={stats.avgRank ? "위" : undefined}
        />
        <StatTile label="미노출" value={stats.trackedCells - stats.exposed} unit="건" />
      </div>

      {canManage && (
        <KeywordForm
          projectId={project.id}
          maxEngines={plan.maxEnginesPerKeyword}
          liveEngines={liveEngines}
        />
      )}

      <section className="card">
        <div className="border-b border-line px-5 py-4">
          <h2 className="font-bold">키워드 순위</h2>
        </div>
        <RankTable rows={rows} />
      </section>
    </div>
  );
}
