import "server-only";

import type { SearchEngine } from "@prisma/client";

import { ENGINES } from "../engines";
import { lookupDaum, daumConfigured } from "./daum";
import { lookupDemo } from "./demo";
import { lookupGoogle, googleConfigured, googleMaxRank } from "./google";
import { lookupNaver, naverConfigured } from "./naver";
import type { LookupInput, RankLookup } from "./types";

export type { RankLookup } from "./types";

/** 해당 검색영역이 실제 API 로 동작 가능한지 */
export function isEngineLive(engine: SearchEngine): boolean {
  switch (ENGINES[engine].provider) {
    case "naver":
      return naverConfigured();
    case "google":
      return googleConfigured();
    case "daum":
      return daumConfigured();
    default:
      return false;
  }
}

export function engineStatuses(): { engine: SearchEngine; live: boolean }[] {
  return (Object.keys(ENGINES) as SearchEngine[]).map((engine) => ({
    engine,
    live: isEngineLive(engine),
  }));
}

export function maxRankFor(engine: SearchEngine): number {
  if (engine === "GOOGLE") return Math.min(ENGINES[engine].maxRank, googleMaxRank());
  return ENGINES[engine].maxRank;
}

/**
 * 단일 (키워드 × 검색영역) 순위 조회.
 * API 키가 없거나 호출이 실패하면 데모 값으로 대체해 화면이 비지 않도록 한다.
 */
export async function lookupRank(args: {
  keyword: string;
  targetDomain: string;
  engine: SearchEngine;
  now?: Date;
}): Promise<RankLookup> {
  const now = args.now ?? new Date();
  const input: LookupInput = {
    keyword: args.keyword,
    targetDomain: args.targetDomain,
    engine: args.engine,
    maxRank: maxRankFor(args.engine),
  };

  if (!isEngineLive(args.engine)) {
    return lookupDemo(input, now);
  }

  try {
    switch (ENGINES[args.engine].provider) {
      case "naver":
        return await lookupNaver(input);
      case "google":
        return await lookupGoogle(input);
      case "daum":
        return await lookupDaum(input);
      default:
        return lookupDemo(input, now);
    }
  } catch (error) {
    console.error(`[collector] ${args.engine} 조회 실패`, error);
    return lookupDemo(input, now);
  }
}
