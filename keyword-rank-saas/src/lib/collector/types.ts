import type { RankSource, SearchEngine } from "@prisma/client";

export type RankLookup = {
  /** 1부터 시작하는 노출 순위. 조회 범위 안에 없으면 null */
  rank: number | null;
  url: string | null;
  title: string | null;
  source: RankSource;
};

export type LookupInput = {
  keyword: string;
  /** 순위 판정 기준 도메인 (예: "bionlifescience.com") */
  targetDomain: string;
  engine: SearchEngine;
  maxRank: number;
};

export const NOT_FOUND: Omit<RankLookup, "source"> = {
  rank: null,
  url: null,
  title: null,
};

/** 검색결과 URL 이 대상 도메인인지 판정 (www., 서브도메인, 프로토콜 무시) */
export function matchesDomain(link: string, targetDomain: string): boolean {
  const target = normalizeDomain(targetDomain);
  if (!target) return false;
  try {
    const host = normalizeDomain(new URL(link).hostname);
    return host === target || host.endsWith(`.${target}`);
  } catch {
    return false;
  }
}

export function normalizeDomain(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .replace(/^https?:\/\//, "")
    .replace(/^www\./, "")
    .split("/")[0]
    .split(":")[0];
}

/** 네이버 API 가 돌려주는 <b> 태그 등을 제거 */
export function stripTags(value: string | undefined | null): string | null {
  if (!value) return null;
  return value
    .replace(/<[^>]*>/g, "")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .trim();
}

/** 외부 API 호출 공통 래퍼 (타임아웃 10초) */
export async function fetchJson<T>(
  url: string,
  init?: RequestInit,
): Promise<T> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 10_000);
  try {
    const res = await fetch(url, {
      ...init,
      signal: controller.signal,
      cache: "no-store",
    });
    if (!res.ok) {
      const body = await res.text().catch(() => "");
      throw new Error(
        `검색 API 오류 (${res.status}) ${body.slice(0, 200)}`.trim(),
      );
    }
    return (await res.json()) as T;
  } finally {
    clearTimeout(timer);
  }
}
