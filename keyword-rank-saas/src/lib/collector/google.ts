import type { LookupInput, RankLookup } from "./types";
import { NOT_FOUND, fetchJson, matchesDomain } from "./types";

type GoogleItem = { link?: string; title?: string };
type GoogleResponse = { items?: GoogleItem[] };

const PAGE_SIZE = 10; // Custom Search JSON API 는 1회 최대 10건

export function googleConfigured(): boolean {
  return Boolean(process.env.GOOGLE_API_KEY && process.env.GOOGLE_SEARCH_ENGINE_ID);
}

/** 무료 할당량(1일 100쿼리)을 고려해 기본 조회 깊이를 30위로 둔다. */
export function googleMaxRank(): number {
  const raw = Number(process.env.GOOGLE_MAX_RANK ?? 30);
  if (!Number.isFinite(raw)) return 30;
  return Math.min(Math.max(Math.floor(raw), PAGE_SIZE), 100);
}

/**
 * 구글 Programmable Search(Custom Search JSON API)로 순위 조회.
 * 10건씩 페이지를 넘기며 대상 도메인을 찾는다.
 */
export async function lookupGoogle(input: LookupInput): Promise<RankLookup> {
  const depth = Math.min(input.maxRank, googleMaxRank());
  const pages = Math.ceil(depth / PAGE_SIZE);

  for (let page = 0; page < pages; page += 1) {
    const start = page * PAGE_SIZE + 1;
    const url =
      "https://www.googleapis.com/customsearch/v1" +
      `?key=${encodeURIComponent(process.env.GOOGLE_API_KEY ?? "")}` +
      `&cx=${encodeURIComponent(process.env.GOOGLE_SEARCH_ENGINE_ID ?? "")}` +
      `&q=${encodeURIComponent(input.keyword)}` +
      `&num=${PAGE_SIZE}&start=${start}&hl=ko&gl=kr`;

    const json = await fetchJson<GoogleResponse>(url);
    const items = json.items ?? [];
    if (items.length === 0) break;

    for (let i = 0; i < items.length; i += 1) {
      const link = items[i]?.link;
      if (link && matchesDomain(link, input.targetDomain)) {
        return {
          rank: start + i,
          url: link,
          title: items[i]?.title ?? null,
          source: "API",
        };
      }
    }
  }
  return { ...NOT_FOUND, source: "API" };
}
