import type { PlanTier } from "@prisma/client";

export type PlanSpec = {
  tier: PlanTier;
  label: string;
  priceMonthly: number; // 원(KRW), 0 = 무료
  maxProjects: number;
  maxKeywords: number; // 조직 전체 키워드 수 상한
  maxEnginesPerKeyword: number;
  autoDailyCollect: boolean; // 매일 자동 수집 대상 포함 여부
  manualChecksPerDay: number; // 수동 "지금 확인" 1일 허용 횟수
  csvExport: boolean;
  seats: number;
  highlights: string[];
};

export const PLANS: Record<PlanTier, PlanSpec> = {
  FREE: {
    tier: "FREE",
    label: "Free",
    priceMonthly: 0,
    maxProjects: 1,
    maxKeywords: 10,
    maxEnginesPerKeyword: 2,
    autoDailyCollect: false,
    manualChecksPerDay: 20,
    csvExport: false,
    seats: 1,
    highlights: [
      "프로젝트 1개 · 키워드 10개",
      "키워드당 검색영역 2개",
      "수동 순위 확인 하루 20회",
      "순위 이력 30일 보관",
    ],
  },
  PRO: {
    tier: "PRO",
    label: "Pro",
    priceMonthly: 29000,
    maxProjects: 5,
    maxKeywords: 200,
    maxEnginesPerKeyword: 4,
    autoDailyCollect: true,
    manualChecksPerDay: 300,
    csvExport: true,
    seats: 5,
    highlights: [
      "프로젝트 5개 · 키워드 200개",
      "네이버·구글·다음 전체 검색영역",
      "매일 자동 수집 + 순위 변동 리포트",
      "CSV 내보내기 · 팀원 5명",
    ],
  },
  BUSINESS: {
    tier: "BUSINESS",
    label: "Business",
    priceMonthly: 99000,
    maxProjects: 30,
    maxKeywords: 2000,
    maxEnginesPerKeyword: 4,
    autoDailyCollect: true,
    manualChecksPerDay: 3000,
    csvExport: true,
    seats: 30,
    highlights: [
      "프로젝트 30개 · 키워드 2,000개",
      "대행사용 다중 사이트 관리",
      "매일 자동 수집 + 전체 이력 보관",
      "CSV 내보내기 · 팀원 30명",
    ],
  },
};

export const PLAN_ORDER: PlanTier[] = ["FREE", "PRO", "BUSINESS"];

export function planOf(tier: PlanTier): PlanSpec {
  return PLANS[tier] ?? PLANS.FREE;
}

export function formatPrice(won: number): string {
  return won === 0 ? "무료" : `${won.toLocaleString("ko-KR")}원`;
}
