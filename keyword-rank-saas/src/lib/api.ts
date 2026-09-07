import "server-only";

import type { MemberRole } from "@prisma/client";

import { getCurrentUser, hasRole, type CurrentUser } from "./auth";
import { prisma } from "./db";

/** API 라우트에서 로그인 사용자 확보 */
export async function apiUser(): Promise<CurrentUser> {
  const user = await getCurrentUser();
  if (!user) throw new Error("AUTH:로그인이 필요합니다.");
  return user;
}

/** 최소 권한 확인 */
export async function apiUserWithRole(min: MemberRole): Promise<CurrentUser> {
  const user = await apiUser();
  if (!hasRole(user, min)) {
    throw new Error("FORBIDDEN:이 작업을 수행할 권한이 없습니다.");
  }
  return user;
}

/** 테넌트 격리: 내 조직 소속 프로젝트만 통과시킨다 */
export async function assertOwnedProject(orgId: string, projectId: string) {
  const project = await prisma.project.findFirst({
    where: { id: projectId, orgId },
  });
  if (!project) throw new Error("NOTFOUND:프로젝트를 찾을 수 없습니다.");
  return project;
}

/** 테넌트 격리: 내 조직 소속 키워드만 통과시킨다 */
export async function assertOwnedKeyword(orgId: string, keywordId: string) {
  const keyword = await prisma.keyword.findFirst({
    where: { id: keywordId, project: { orgId } },
    include: { project: true },
  });
  if (!keyword) throw new Error("NOTFOUND:키워드를 찾을 수 없습니다.");
  return keyword;
}
