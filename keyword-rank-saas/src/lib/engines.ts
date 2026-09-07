import type { SearchEngine } from "@prisma/client";

/**
 * 검색영역 메타데이터.
 * color 값은 dataviz 기본 카테고리 팔레트의 슬롯 1~4를 고정 순서로 사용한다.
 * (색은 "엔진"이라는 개체에 고정 - 목록이 필터링돼도 색이 바뀌지 않는다)
 */
export type EngineMeta = {
  key: SearchEngine;
  label: string;
  short: string;
  provider: "naver" | "google" | "daum";
  /** 조회 가능한 최대 순위 (이 범위 밖이면 미노출로 기록) */
  maxRank: number;
  colorVar: string; // CSS 변수명 (globals.css 에서 라이트/다크 정의)
};

export const ENGINES: Record<SearchEngine, EngineMeta> = {
  NAVER_WEB: {
    key: "NAVER_WEB",
    label: "네이버 웹문서",
    short: "N-웹",
    provider: "naver",
    maxRank: 100,
    colorVar: "--series-1",
  },
  NAVER_BLOG: {
    key: "NAVER_BLOG",
    label: "네이버 블로그",
    short: "N-블로그",
    provider: "naver",
    maxRank: 100,
    colorVar: "--series-2",
  },
  GOOGLE: {
    key: "GOOGLE",
    label: "구글",
    short: "구글",
    provider: "google",
    maxRank: 100,
    colorVar: "--series-3",
  },
  DAUM_WEB: {
    key: "DAUM_WEB",
    label: "다음 웹문서",
    short: "다음",
    provider: "daum",
    maxRank: 100,
    colorVar: "--series-4",
  },
};

export const ENGINE_ORDER: SearchEngine[] = [
  "NAVER_WEB",
  "NAVER_BLOG",
  "GOOGLE",
  "DAUM_WEB",
];

export function engineLabel(key: SearchEngine): string {
  return ENGINES[key]?.label ?? String(key);
}

/** 저장 순서를 항상 ENGINE_ORDER 기준으로 정규화 */
export function sortEngines(list: SearchEngine[]): SearchEngine[] {
  const seen = new Set(list);
  return ENGINE_ORDER.filter((e) => seen.has(e));
}
