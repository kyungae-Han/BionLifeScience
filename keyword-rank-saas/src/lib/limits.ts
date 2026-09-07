import "server-only";

import type { Organization } from "@prisma/client";

import { prisma } from "./db";
import { planOf } from "./plan";
import { kstDateOnly } from "./date";

/** 요금제 한도 초과는 LIMIT: 접두사로 던져 API 에서 402 로 변환한다. */
function limitError(message: string): never {
  throw new Error(`LIMIT:${message}`);
}

export async function assertCanAddProject(org: Organization): Promise<void> {
  const plan = planOf(org.plan);
  const count = await prisma.project.count({ where: { orgId: org.id } });
  if (count >= plan.maxProjects) {
    limitError(
      `${plan.label} 요금제는 프로젝트를 ${plan.maxProjects}개까지 만들 수 있습니다. 요금제를 변경해 주세요.`,
    );
  }
}

export async function assertCanAddKeywords(
  org: Organization,
  adding: number,
): Promise<void> {
  const plan = planOf(org.plan);
  const count = await prisma.keyword.count({
    where: { project: { orgId: org.id } },
  });
  if (count + adding > plan.maxKeywords) {
    limitError(
      `${plan.label} 요금제의 키워드 한도(${plan.maxKeywords}개)를 초과합니다. 현재 ${count}개 등록되어 있습니다.`,
    );
  }
}

export function assertEngineCount(org: Organization, engines: unknown[]): void {
  const plan = planOf(org.plan);
  if (engines.length === 0) {
    limitError("검색영역을 최소 1개 선택해 주세요.");
  }
  if (engines.length > plan.maxEnginesPerKeyword) {
    limitError(
      `${plan.label} 요금제는 키워드당 검색영역을 ${plan.maxEnginesPerKeyword}개까지 선택할 수 있습니다.`,
    );
  }
}

/** 오늘 수동 수집으로 소모한 조회 건수 */
export async function manualChecksToday(orgId: string): Promise<number> {
  const since = kstDateOnly();
  const rows = await prisma.collectRun.aggregate({
    where: {
      orgId,
      startedAt: { gte: since },
      triggeredBy: { startsWith: "manual" },
    },
    _sum: { totalCount: true },
  });
  return rows._sum.totalCount ?? 0;
}

export async function assertCanManualCheck(
  org: Organization,
  requesting: number,
): Promise<void> {
  const plan = planOf(org.plan);
  const used = await manualChecksToday(org.id);
  if (used + requesting > plan.manualChecksPerDay) {
    limitError(
      `오늘 수동 순위 확인 한도(${plan.manualChecksPerDay}회)를 초과했습니다. 현재 ${used}회 사용했습니다.`,
    );
  }
}

export async function assertCsvExport(org: Organization): Promise<void> {
  if (!planOf(org.plan).csvExport) {
    limitError("CSV 내보내기는 Pro 요금제부터 사용할 수 있습니다.");
  }
}
