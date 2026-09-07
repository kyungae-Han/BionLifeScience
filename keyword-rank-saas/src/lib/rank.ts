import "server-only";

import type { RunStatus, SearchEngine } from "@prisma/client";

import { prisma } from "./db";
import { lookupRank } from "./collector";
import { addDays, isoDate, kstDateOnly } from "./date";
import { sortEngines } from "./engines";

const CONCURRENCY = 3;

export type CollectTarget = {
  id: string;
  text: string;
  engines: SearchEngine[];
  targetDomain: string;
};

export type CollectSummary = {
  runId: string;
  total: number;
  ok: number;
  fail: number;
};

/** 수집 대상 키워드 목록 조회 (프로젝트/조직 단위) */
export async function loadTargets(where: {
  orgId?: string;
  projectId?: string;
  keywordIds?: string[];
}): Promise<CollectTarget[]> {
  const keywords = await prisma.keyword.findMany({
    where: {
      active: true,
      ...(where.keywordIds ? { id: { in: where.keywordIds } } : {}),
      project: {
        active: true,
        ...(where.projectId ? { id: where.projectId } : {}),
        ...(where.orgId ? { orgId: where.orgId } : {}),
      },
    },
    include: { project: { select: { targetDomain: true } } },
    orderBy: { createdAt: "asc" },
  });

  return keywords.map((k) => ({
    id: k.id,
    text: k.text,
    engines: sortEngines(k.engines),
    targetDomain: k.project.targetDomain,
  }));
}

/** (키워드 × 검색영역) 총 조회 건수 */
export function countChecks(targets: CollectTarget[]): number {
  return targets.reduce((sum, t) => sum + t.engines.length, 0);
}

/**
 * 실제 수집 실행. 키워드×검색영역 조합을 만들고 동시 3건씩 처리한 뒤
 * (키워드, 검색영역, 날짜) 기준으로 upsert 한다 - 하루에 여러 번 돌려도 행이 늘지 않는다.
 */
export async function runCollect(args: {
  targets: CollectTarget[];
  orgId?: string;
  triggeredBy: string;
  now?: Date;
}): Promise<CollectSummary> {
  const now = args.now ?? new Date();
  const checkedOn = kstDateOnly(now);
  const total = countChecks(args.targets);

  const run = await prisma.collectRun.create({
    data: {
      orgId: args.orgId ?? null,
      triggeredBy: args.triggeredBy,
      totalCount: total,
      status: "RUNNING",
    },
  });

  const jobs: { target: CollectTarget; engine: SearchEngine }[] = [];
  for (const target of args.targets) {
    for (const engine of target.engines) jobs.push({ target, engine });
  }

  let ok = 0;
  let fail = 0;

  let cursor = 0;
  async function worker() {
    while (cursor < jobs.length) {
      const job = jobs[cursor];
      cursor += 1;
      try {
        const result = await lookupRank({
          keyword: job.target.text,
          targetDomain: job.target.targetDomain,
          engine: job.engine,
          now,
        });
        await prisma.rankSnapshot.upsert({
          where: {
            keywordId_engine_checkedOn: {
              keywordId: job.target.id,
              engine: job.engine,
              checkedOn,
            },
          },
          create: {
            keywordId: job.target.id,
            engine: job.engine,
            checkedOn,
            rank: result.rank,
            foundUrl: result.url,
            foundTitle: result.title,
            source: result.source,
          },
          update: {
            rank: result.rank,
            foundUrl: result.url,
            foundTitle: result.title,
            source: result.source,
            checkedAt: new Date(),
          },
        });
        ok += 1;
      } catch (error) {
        fail += 1;
        console.error("[collect] 실패", job.target.text, job.engine, error);
      }
    }
  }

  await Promise.all(
    Array.from({ length: Math.min(CONCURRENCY, jobs.length || 1) }, worker),
  );

  const status: RunStatus = fail === 0 ? "SUCCESS" : ok === 0 ? "FAILED" : "SUCCESS";
  await prisma.collectRun.update({
    where: { id: run.id },
    data: {
      status,
      okCount: ok,
      failCount: fail,
      finishedAt: new Date(),
      message: fail > 0 ? `${fail}건 실패` : null,
    },
  });

  return { runId: run.id, total, ok, fail };
}

/* ────────────────────────────── 조회용 ────────────────────────────── */

export type EngineCell = {
  engine: SearchEngine;
  rank: number | null;
  prevRank: number | null;
  /** 이전 대비 상승폭(+면 순위 상승). 비교 대상이 없으면 null */
  delta: number | null;
  url: string | null;
  checkedOn: Date | null;
};

export type KeywordRow = {
  id: string;
  text: string;
  projectId: string;
  projectName: string;
  cells: EngineCell[];
  bestRank: number | null;
};

/** 키워드 표에 필요한 최신/직전 순위를 한 번에 계산 */
export async function buildKeywordRows(where: {
  orgId: string;
  projectId?: string;
}): Promise<KeywordRow[]> {
  const keywords = await prisma.keyword.findMany({
    where: {
      project: {
        orgId: where.orgId,
        ...(where.projectId ? { id: where.projectId } : {}),
      },
    },
    include: { project: { select: { id: true, name: true } } },
    orderBy: { createdAt: "asc" },
  });
  if (keywords.length === 0) return [];

  const snapshots = await prisma.rankSnapshot.findMany({
    where: { keywordId: { in: keywords.map((k) => k.id) } },
    orderBy: { checkedOn: "desc" },
    select: {
      keywordId: true,
      engine: true,
      rank: true,
      foundUrl: true,
      checkedOn: true,
    },
  });

  // keywordId|engine -> 최신순 스냅샷
  const grouped = new Map<string, typeof snapshots>();
  for (const snap of snapshots) {
    const key = `${snap.keywordId}|${snap.engine}`;
    const list = grouped.get(key);
    if (list) list.push(snap);
    else grouped.set(key, [snap]);
  }

  return keywords.map((keyword) => {
    const cells: EngineCell[] = sortEngines(keyword.engines).map((engine) => {
      const list = grouped.get(`${keyword.id}|${engine}`) ?? [];
      const latest = list[0];
      const prev = list[1];
      const rank = latest?.rank ?? null;
      const prevRank = prev?.rank ?? null;
      return {
        engine,
        rank,
        prevRank,
        delta: rank !== null && prevRank !== null ? prevRank - rank : null,
        url: latest?.foundUrl ?? null,
        checkedOn: latest?.checkedOn ?? null,
      };
    });

    const ranked = cells.map((c) => c.rank).filter((r): r is number => r !== null);
    return {
      id: keyword.id,
      text: keyword.text,
      projectId: keyword.project.id,
      projectName: keyword.project.name,
      cells,
      bestRank: ranked.length ? Math.min(...ranked) : null,
    };
  });
}

export type Overview = {
  keywordCount: number;
  projectCount: number;
  trackedCells: number;
  top10: number;
  exposed: number;
  avgRank: number | null;
  up: number;
  down: number;
  lastCollectedAt: Date | null;
};

export function summarize(rows: KeywordRow[]): Omit<Overview, "projectCount" | "lastCollectedAt"> {
  let trackedCells = 0;
  let top10 = 0;
  let exposed = 0;
  let up = 0;
  let down = 0;
  const ranks: number[] = [];

  for (const row of rows) {
    for (const cell of row.cells) {
      trackedCells += 1;
      if (cell.rank !== null) {
        exposed += 1;
        ranks.push(cell.rank);
        if (cell.rank <= 10) top10 += 1;
      }
      if (cell.delta !== null && cell.delta > 0) up += 1;
      if (cell.delta !== null && cell.delta < 0) down += 1;
    }
  }

  return {
    keywordCount: rows.length,
    trackedCells,
    top10,
    exposed,
    avgRank: ranks.length
      ? Math.round((ranks.reduce((a, b) => a + b, 0) / ranks.length) * 10) / 10
      : null,
    up,
    down,
  };
}

export type HistorySeries = {
  engine: SearchEngine;
  points: { date: string; rank: number | null }[];
};

/** 키워드 상세 차트용 시계열 (없는 날은 null 로 채워 축을 일정하게 유지) */
export async function getKeywordHistory(
  keywordId: string,
  days = 30,
): Promise<{ dates: string[]; series: HistorySeries[] }> {
  const today = kstDateOnly();
  const from = addDays(today, -(days - 1));

  const keyword = await prisma.keyword.findUnique({
    where: { id: keywordId },
    select: { engines: true },
  });
  const engines = sortEngines(keyword?.engines ?? []);

  const snapshots = await prisma.rankSnapshot.findMany({
    where: { keywordId, checkedOn: { gte: from } },
    orderBy: { checkedOn: "asc" },
    select: { engine: true, rank: true, checkedOn: true },
  });

  const dates: string[] = [];
  for (let i = 0; i < days; i += 1) dates.push(isoDate(addDays(from, i)));

  const byEngine = new Map<string, Map<string, number | null>>();
  for (const snap of snapshots) {
    const key = String(snap.engine);
    const map = byEngine.get(key) ?? new Map<string, number | null>();
    map.set(isoDate(snap.checkedOn), snap.rank);
    byEngine.set(key, map);
  }

  const series: HistorySeries[] = engines.map((engine) => {
    const map = byEngine.get(String(engine)) ?? new Map();
    return {
      engine,
      points: dates.map((date) => ({
        date,
        rank: map.has(date) ? (map.get(date) ?? null) : null,
      })),
    };
  });

  return { dates, series };
}
