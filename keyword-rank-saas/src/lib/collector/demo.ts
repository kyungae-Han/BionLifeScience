import type { LookupInput, RankLookup } from "./types";

/**
 * API 키가 없을 때 사용하는 데모 수집기.
 * (키워드 + 검색영역 + 도메인 + 날짜)로부터 결정적으로 순위를 만들어내므로
 * 새로고침해도 값이 흔들리지 않고, 날짜가 지나면 자연스럽게 등락한다.
 */
export function lookupDemo(input: LookupInput, date: Date): RankLookup {
  const seed = hash(`${input.keyword}|${input.engine}|${input.targetDomain}`);
  const day = Math.floor(date.getTime() / 86_400_000);

  const base = 3 + (seed % 38); // 3~40위 사이에서 출발
  const wave = Math.round(5 * Math.sin(day / 4 + (seed % 100) / 16));
  const jitter = Math.round(random(seed + day) * 5) - 2;
  const rank = base + wave + jitter;

  if (rank < 1) {
    return { rank: 1, url: demoUrl(input), title: demoTitle(input), source: "DEMO" };
  }
  if (rank > input.maxRank) {
    return { rank: null, url: null, title: null, source: "DEMO" };
  }
  return { rank, url: demoUrl(input), title: demoTitle(input), source: "DEMO" };
}

function demoUrl(input: LookupInput): string {
  return `https://${input.targetDomain}/?q=${encodeURIComponent(input.keyword)}`;
}

function demoTitle(input: LookupInput): string {
  return `${input.keyword} | ${input.targetDomain}`;
}

function hash(value: string): number {
  let h = 2166136261;
  for (let i = 0; i < value.length; i += 1) {
    h ^= value.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  return Math.abs(h);
}

function random(seed: number): number {
  const x = Math.sin(seed) * 10000;
  return x - Math.floor(x);
}
