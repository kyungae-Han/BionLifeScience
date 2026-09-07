import Link from "next/link";
import { notFound } from "next/navigation";

import CollectButton from "@/components/CollectButton";
import DeleteButton from "@/components/DeleteButton";
import RankChart from "@/components/RankChart";
import { requireUser } from "@/lib/auth";
import { ENGINES } from "@/lib/engines";
import { prisma } from "@/lib/db";
import { buildKeywordRows, getKeywordHistory } from "@/lib/rank";

export const dynamic = "force-dynamic";

const HISTORY_DAYS = 30;

export default async function KeywordDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const user = await requireUser();

  const keyword = await prisma.keyword.findFirst({
    where: { id, project: { orgId: user.orgId } },
    include: { project: true },
  });
  if (!keyword) notFound();

  const [history, rows] = await Promise.all([
    getKeywordHistory(id, HISTORY_DAYS),
    buildKeywordRows({ orgId: user.orgId, projectId: keyword.projectId }),
  ]);
  const row = rows.find((r) => r.id === id);
  const canManage = user.role !== "MEMBER";

  // 표 보기: 값이 하나라도 있는 날짜만 최신순으로
  const tableDates = history.dates
    .map((date, index) => ({ date, index }))
    .filter(({ index }) => history.series.some((s) => s.points[index].rank !== null))
    .reverse();

  return (
    <div className="space-y-6">
      <div>
        <Link
          href={`/app/projects/${keyword.projectId}`}
          className="text-sm text-muted hover:underline"
        >
          ← {keyword.project.name}
        </Link>
        <div className="mt-3 flex flex-wrap items-end justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold">{keyword.text}</h1>
            <p className="mt-1 text-sm text-ink-2">
              {keyword.project.targetDomain} 기준 · 최근 {HISTORY_DAYS}일
            </p>
          </div>
          <div className="flex items-center gap-2">
            <CollectButton keywordId={keyword.id} label="이 키워드만 확인" />
            {canManage && (
              <DeleteButton
                endpoint={`/api/keywords/${keyword.id}`}
                confirmMessage={`'${keyword.text}' 키워드와 순위 이력이 삭제됩니다. 계속할까요?`}
                redirectTo={`/app/projects/${keyword.projectId}`}
              />
            )}
          </div>
        </div>
      </div>

      {/* 검색영역별 현재 순위 */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {row?.cells.map((cell) => (
          <div key={cell.engine} className="card p-5">
            <p className="flex items-center gap-1.5 text-sm font-semibold text-ink-2">
              <span
                aria-hidden
                className="inline-block h-2.5 w-2.5 rounded-full"
                style={{ background: `var(${ENGINES[cell.engine].colorVar})` }}
              />
              {ENGINES[cell.engine].label}
            </p>
            <p className="num mt-2 text-3xl font-bold">
              {cell.rank === null ? (
                <span className="text-muted">미노출</span>
              ) : (
                <>
                  {cell.rank}
                  <span className="ml-1 text-base font-semibold text-muted">위</span>
                </>
              )}
            </p>
            {cell.delta !== null && cell.delta !== 0 && (
              <p
                className="num mt-1 text-xs font-semibold"
                style={{
                  color: cell.delta > 0 ? "var(--good-text)" : "var(--critical)",
                }}
              >
                {cell.delta > 0 ? "▲" : "▼"} {Math.abs(cell.delta)} (직전 대비)
              </p>
            )}
            {cell.url && (
              <a
                href={cell.url}
                target="_blank"
                rel="noreferrer noopener"
                className="mt-2 block truncate text-xs text-muted hover:underline"
                title={cell.url}
              >
                {cell.url}
              </a>
            )}
          </div>
        ))}
      </div>

      <section className="card p-5">
        <h2 className="font-bold">순위 추이</h2>
        <div className="mt-4">
          <RankChart dates={history.dates} series={history.series} />
        </div>
      </section>

      <section className="card">
        <div className="border-b border-line px-5 py-4">
          <h2 className="font-bold">수집 이력</h2>
        </div>
        {tableDates.length === 0 ? (
          <p className="px-5 py-10 text-center text-sm text-muted">
            아직 수집된 순위가 없습니다. 위의 &quot;이 키워드만 확인&quot;을 눌러 보세요.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="table">
              <thead>
                <tr>
                  <th>날짜</th>
                  {history.series.map((s) => (
                    <th key={s.engine}>
                      <span className="inline-flex items-center gap-1.5">
                        <span
                          aria-hidden
                          className="inline-block h-2.5 w-2.5 rounded-full"
                          style={{ background: `var(${ENGINES[s.engine].colorVar})` }}
                        />
                        {ENGINES[s.engine].label}
                      </span>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {tableDates.map(({ date, index }) => (
                  <tr key={date}>
                    <td className="num">{date}</td>
                    {history.series.map((s) => (
                      <td key={s.engine} className="num">
                        {s.points[index].rank === null ? (
                          <span className="text-muted">미노출</span>
                        ) : (
                          `${s.points[index].rank}위`
                        )}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
