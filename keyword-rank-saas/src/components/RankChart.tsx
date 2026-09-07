"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import type { SearchEngine } from "@prisma/client";

import { ENGINES } from "@/lib/engines";

export type ChartSeries = {
  engine: SearchEngine;
  points: { date: string; rank: number | null }[];
};

const PAD = { top: 18, right: 78, bottom: 30, left: 46 };
const HEIGHT = 300;

/**
 * 순위 추이 선그래프.
 * - y축은 위쪽이 1위(값이 작을수록 좋음)라 반전해서 그린다.
 * - 미노출(null)인 날은 선을 끊어 "0위"로 오해되지 않게 한다.
 * - 색 대비가 낮은 라이트 모드를 위해 끝점 직접 라벨 + 아래 표를 함께 제공한다.
 */
export default function RankChart({
  dates,
  series,
}: {
  dates: string[];
  series: ChartSeries[];
}) {
  const wrapRef = useRef<HTMLDivElement>(null);
  const [width, setWidth] = useState(760);
  const [hover, setHover] = useState<number | null>(null);

  useEffect(() => {
    const el = wrapRef.current;
    if (!el) return;
    const observer = new ResizeObserver(([entry]) => {
      setWidth(Math.max(320, Math.floor(entry.contentRect.width)));
    });
    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  const maxRank = useMemo(() => {
    const ranks = series.flatMap((s) =>
      s.points.map((p) => p.rank).filter((r): r is number => r !== null),
    );
    if (ranks.length === 0) return 20;
    return Math.max(10, Math.ceil(Math.max(...ranks) / 10) * 10);
  }, [series]);

  const innerW = Math.max(1, width - PAD.left - PAD.right);
  const innerH = HEIGHT - PAD.top - PAD.bottom;
  const stepX = dates.length > 1 ? innerW / (dates.length - 1) : 0;

  const x = (i: number) => PAD.left + i * stepX;
  const y = (rank: number) =>
    PAD.top + ((rank - 1) / Math.max(1, maxRank - 1)) * innerH;

  const yTicks = useMemo(() => {
    const ticks = [1];
    const step = Math.max(1, Math.round(maxRank / 4));
    for (let v = step; v <= maxRank; v += step) ticks.push(v);
    if (ticks[ticks.length - 1] !== maxRank) ticks.push(maxRank);
    return Array.from(new Set(ticks));
  }, [maxRank]);

  const xTickIdx = useMemo(() => {
    const count = Math.min(6, dates.length);
    if (count <= 1) return [0];
    const gap = (dates.length - 1) / (count - 1);
    return Array.from({ length: count }, (_, i) => Math.round(i * gap));
  }, [dates.length]);

  // 끝점 라벨: 겹치면 세로로 밀어내고 연결선을 그린다
  const endLabels = useMemo(() => {
    const items = series
      .map((s) => {
        for (let i = s.points.length - 1; i >= 0; i -= 1) {
          const rank = s.points[i].rank;
          if (rank !== null) return { engine: s.engine, i, rank, y: y(rank) };
        }
        return null;
      })
      .filter((v): v is { engine: SearchEngine; i: number; rank: number; y: number } => v !== null)
      .sort((a, b) => a.y - b.y);

    let last = -Infinity;
    return items.map((item) => {
      const labelY = Math.max(item.y, last + 15);
      last = labelY;
      return { ...item, labelY };
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [series, maxRank, innerH]);

  function pathFor(points: { rank: number | null }[]): string {
    let d = "";
    let pen = false;
    points.forEach((p, i) => {
      if (p.rank === null) {
        pen = false;
        return;
      }
      d += `${pen ? "L" : "M"}${x(i).toFixed(1)},${y(p.rank).toFixed(1)} `;
      pen = true;
    });
    return d.trim();
  }

  function onMove(event: React.MouseEvent<SVGSVGElement>) {
    const rect = event.currentTarget.getBoundingClientRect();
    const px = event.clientX - rect.left;
    if (stepX === 0) {
      setHover(0);
      return;
    }
    const idx = Math.round((px - PAD.left) / stepX);
    setHover(Math.min(dates.length - 1, Math.max(0, idx)));
  }

  const hasData = series.some((s) => s.points.some((p) => p.rank !== null));

  return (
    <div ref={wrapRef} className="relative w-full">
      {/* 범례: 2개 이상 시리즈에서는 항상 표시 */}
      {series.length > 1 && (
        <div className="mb-3 flex flex-wrap gap-x-4 gap-y-1.5">
          {series.map((s) => (
            <span key={s.engine} className="inline-flex items-center gap-1.5 text-xs text-ink-2">
              <span
                aria-hidden
                className="inline-block h-2.5 w-2.5 rounded-full"
                style={{ background: `var(${ENGINES[s.engine].colorVar})` }}
              />
              {ENGINES[s.engine].label}
            </span>
          ))}
        </div>
      )}

      <svg
        width={width}
        height={HEIGHT}
        role="img"
        aria-label="검색 순위 추이 그래프"
        onMouseMove={onMove}
        onMouseLeave={() => setHover(null)}
        style={{ display: "block", maxWidth: "100%" }}
      >
        {/* 가로 격자 + y축 눈금 (1위가 위) */}
        {yTicks.map((tick) => (
          <g key={tick}>
            <line
              x1={PAD.left}
              x2={PAD.left + innerW}
              y1={y(tick)}
              y2={y(tick)}
              stroke="var(--line)"
              strokeWidth={1}
            />
            <text
              x={PAD.left - 10}
              y={y(tick) + 4}
              textAnchor="end"
              fontSize={11}
              fill="var(--muted)"
              style={{ fontVariantNumeric: "tabular-nums" }}
            >
              {tick}위
            </text>
          </g>
        ))}

        {/* x축 날짜 */}
        {xTickIdx.map((i) => (
          <text
            key={i}
            x={x(i)}
            y={HEIGHT - 10}
            textAnchor="middle"
            fontSize={11}
            fill="var(--muted)"
            style={{ fontVariantNumeric: "tabular-nums" }}
          >
            {dates[i]?.slice(5).replace("-", ".")}
          </text>
        ))}

        {/* 호버 크로스헤어 */}
        {hover !== null && hasData && (
          <line
            x1={x(hover)}
            x2={x(hover)}
            y1={PAD.top}
            y2={PAD.top + innerH}
            stroke="var(--axis)"
            strokeWidth={1}
          />
        )}

        {/* 시리즈 */}
        {series.map((s) => (
          <path
            key={s.engine}
            d={pathFor(s.points)}
            fill="none"
            stroke={`var(${ENGINES[s.engine].colorVar})`}
            strokeWidth={2}
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        ))}

        {/* 호버 지점 마커 (표면색 링으로 겹침에도 읽히게) */}
        {hover !== null &&
          series.map((s) => {
            const rank = s.points[hover]?.rank;
            if (rank === null || rank === undefined) return null;
            return (
              <circle
                key={s.engine}
                cx={x(hover)}
                cy={y(rank)}
                r={4.5}
                fill={`var(${ENGINES[s.engine].colorVar})`}
                stroke="var(--surface)"
                strokeWidth={2}
              />
            );
          })}

        {/* 끝점 마커 + 직접 라벨 (겹치면 연결선) */}
        {endLabels.map((item) => (
          <g key={item.engine}>
            <circle
              cx={x(item.i)}
              cy={item.y}
              r={4.5}
              fill={`var(${ENGINES[item.engine].colorVar})`}
              stroke="var(--surface)"
              strokeWidth={2}
            />
            {Math.abs(item.labelY - item.y) > 1 && (
              <line
                x1={x(item.i) + 6}
                y1={item.y}
                x2={x(item.i) + 12}
                y2={item.labelY - 4}
                stroke="var(--axis)"
                strokeWidth={1}
              />
            )}
            <text
              x={x(item.i) + 14}
              y={item.labelY}
              fontSize={11}
              fill="var(--ink-2)"
              style={{ fontVariantNumeric: "tabular-nums" }}
            >
              {ENGINES[item.engine].short} {item.rank}위
            </text>
          </g>
        ))}

        {!hasData && (
          <text
            x={PAD.left + innerW / 2}
            y={PAD.top + innerH / 2}
            textAnchor="middle"
            fontSize={13}
            fill="var(--muted)"
          >
            아직 수집된 순위가 없습니다
          </text>
        )}
      </svg>

      {/* 툴팁 */}
      {hover !== null && hasData && (
        <div
          className="card pointer-events-none absolute p-3 text-xs shadow-lg"
          style={{
            // 커서 반대편에 붙여 그래프 선을 가리지 않게 한다
            ...(x(hover) < width / 2 ? { right: 8 } : { left: 8 }),
            top: series.length > 1 ? 30 : 8,
            minWidth: 140,
          }}
        >
          <p className="font-semibold">{dates[hover]}</p>
          <ul className="mt-2 space-y-1">
            {series.map((s) => (
              <li key={s.engine} className="flex items-center justify-between gap-3">
                <span className="inline-flex items-center gap-1.5 text-ink-2">
                  <span
                    aria-hidden
                    className="inline-block h-2 w-2 rounded-full"
                    style={{ background: `var(${ENGINES[s.engine].colorVar})` }}
                  />
                  {ENGINES[s.engine].short}
                </span>
                <span className="num font-semibold">
                  {s.points[hover]?.rank === null || s.points[hover]?.rank === undefined
                    ? "미노출"
                    : `${s.points[hover].rank}위`}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}

      <p className="mt-2 text-xs text-muted">
        위로 갈수록 상위 노출입니다. 선이 끊긴 구간은 해당 날짜에 검색 결과에서 찾지 못한
        경우입니다.
      </p>
    </div>
  );
}
