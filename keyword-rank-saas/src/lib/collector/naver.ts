import type { LookupInput, RankLookup } from "./types";
import { NOT_FOUND, fetchJson, matchesDomain, stripTags } from "./types";

type NaverItem = { title?: string; link?: string };
type NaverResponse = { items?: NaverItem[]; total?: number };

const DISPLAY = 100; // 네이버 검색 API 1회 최대 건수

export function naverConfigured(): boolean {
  return Boolean(process.env.NAVER_CLIENT_ID && process.env.NAVER_CLIENT_SECRET);
}

/**
 * 네이버 검색 API(웹문서/블로그)로 순위 조회.
 * - 공식 API 한 번 호출로 1~100위까지 확인한다.
 * - 통합검색(스마트블록) 노출 순위와는 다를 수 있음: README 참고.
 */
export async function lookupNaver(input: LookupInput): Promise<RankLookup> {
  const path = input.engine === "NAVER_BLOG" ? "blog" : "webkr";
  const url =
    `https://openapi.naver.com/v1/search/${path}.json` +
    `?query=${encodeURIComponent(input.keyword)}` +
    `&display=${DISPLAY}&start=1`;

  const json = await fetchJson<NaverResponse>(url, {
    headers: {
      "X-Naver-Client-Id": process.env.NAVER_CLIENT_ID ?? "",
      "X-Naver-Client-Secret": process.env.NAVER_CLIENT_SECRET ?? "",
    },
  });

  const items = json.items ?? [];
  const limit = Math.min(items.length, input.maxRank);
  for (let i = 0; i < limit; i += 1) {
    const link = items[i]?.link;
    if (link && matchesDomain(link, input.targetDomain)) {
      return {
        rank: i + 1,
        url: link,
        title: stripTags(items[i]?.title),
        source: "API",
      };
    }
  }
  return { ...NOT_FOUND, source: "API" };
}
