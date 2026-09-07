import type { EngineCell } from "@/lib/rank";

/** 순위 + 변동을 함께 보여준다. 방향은 기호(▲▼)로도 표시해 색에만 의존하지 않는다. */
export default function RankBadge({ cell }: { cell: EngineCell }) {
  if (cell.rank === null) {
    return (
      <span className="num text-sm text-muted">
        미노출
        {cell.prevRank !== null && (
          <span className="ml-1" style={{ color: "var(--critical)" }}>
            ▼ 이탈
          </span>
        )}
      </span>
    );
  }

  return (
    <span className="num inline-flex items-baseline gap-1.5 text-sm">
      <strong className="text-[15px]">{cell.rank}</strong>
      <span className="text-muted">위</span>
      {cell.delta !== null && cell.delta !== 0 && (
        <span
          className="text-xs font-semibold"
          style={{
            color: cell.delta > 0 ? "var(--good-text)" : "var(--critical)",
          }}
        >
          {cell.delta > 0 ? "▲" : "▼"}
          {Math.abs(cell.delta)}
        </span>
      )}
      {cell.delta === 0 && <span className="text-xs text-muted">–</span>}
    </span>
  );
}
