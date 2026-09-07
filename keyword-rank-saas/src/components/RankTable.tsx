import Link from "next/link";

import { ENGINES, ENGINE_ORDER } from "@/lib/engines";
import type { KeywordRow } from "@/lib/rank";
import { isoDate } from "@/lib/date";
import RankBadge from "./RankBadge";

/**
 * 키워드 × 검색영역 순위 표.
 * 차트의 색 대비가 낮은 라이트 모드에서도 값을 읽을 수 있도록,
 * 이 표가 그래프의 "표 보기" 역할을 겸한다.
 */
export default function RankTable({
  rows,
  showProject = false,
}: {
  rows: KeywordRow[];
  showProject?: boolean;
}) {
  if (rows.length === 0) {
    return (
      <p className="px-4 py-10 text-center text-sm text-muted">
        아직 등록된 키워드가 없습니다.
      </p>
    );
  }

  const used = new Set(rows.flatMap((r) => r.cells.map((c) => c.engine)));
  const columns = ENGINE_ORDER.filter((e) => used.has(e));

  return (
    <div className="overflow-x-auto">
      <table className="table">
        <thead>
          <tr>
            <th style={{ minWidth: 180 }}>키워드</th>
            {showProject && <th>프로젝트</th>}
            {columns.map((engine) => (
              <th key={engine}>
                <span className="inline-flex items-center gap-1.5">
                  <span
                    aria-hidden
                    className="inline-block h-2.5 w-2.5 rounded-full"
                    style={{ background: `var(${ENGINES[engine].colorVar})` }}
                  />
                  {ENGINES[engine].label}
                </span>
              </th>
            ))}
            <th>최근 수집</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const latest = row.cells
              .map((c) => c.checkedOn)
              .filter((d): d is Date => Boolean(d))
              .sort((a, b) => b.getTime() - a.getTime())[0];

            return (
              <tr key={row.id}>
                <td>
                  <Link
                    href={`/app/keywords/${row.id}`}
                    className="font-semibold hover:underline"
                  >
                    {row.text}
                  </Link>
                </td>
                {showProject && (
                  <td className="text-sm text-ink-2">{row.projectName}</td>
                )}
                {columns.map((engine) => {
                  const cell = row.cells.find((c) => c.engine === engine);
                  return (
                    <td key={engine}>
                      {cell ? (
                        <RankBadge cell={cell} />
                      ) : (
                        <span className="text-sm text-muted">–</span>
                      )}
                    </td>
                  );
                })}
                <td className="num text-sm text-muted">
                  {latest ? isoDate(latest) : "-"}
                </td>
                <td className="text-right">
                  <Link href={`/app/keywords/${row.id}`} className="btn btn-sm">
                    추이
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
