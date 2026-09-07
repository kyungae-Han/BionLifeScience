import type { LookupInput, RankLookup } from "./types";
import { NOT_FOUND, fetchJson, matchesDomain, stripTags } from "./types";

type KakaoDoc = { url?: string; title?: string };
type KakaoResponse = { documents?: KakaoDoc[]; meta?: { is_end?: boolean } };

const PAGE_SIZE = 50; // 카카오 검색 API 1회 최대 건수

export function daumConfigured(): boolean {
  return Boolean(process.env.KAKAO_REST_API_KEY);
}

/** 카카오(다음) 웹문서 검색 API 로 순위 조회. 50건씩 최대 100위까지. */
export async function lookupDaum(input: LookupInput): Promise<RankLookup> {
  const pages = Math.ceil(Math.min(input.maxRank, 100) / PAGE_SIZE);

  for (let page = 1; page <= pages; page += 1) {
    const url =
      "https://dapi.kakao.com/v2/search/web" +
      `?query=${encodeURIComponent(input.keyword)}` +
      `&size=${PAGE_SIZE}&page=${page}`;

    const json = await fetchJson<KakaoResponse>(url, {
      headers: { Authorization: `KakaoAK ${process.env.KAKAO_REST_API_KEY ?? ""}` },
    });

    const docs = json.documents ?? [];
    for (let i = 0; i < docs.length; i += 1) {
      const link = docs[i]?.url;
      if (link && matchesDomain(link, input.targetDomain)) {
        return {
          rank: (page - 1) * PAGE_SIZE + i + 1,
          url: link,
          title: stripTags(docs[i]?.title),
          source: "API",
        };
      }
    }
    if (json.meta?.is_end) break;
  }
  return { ...NOT_FOUND, source: "API" };
}
