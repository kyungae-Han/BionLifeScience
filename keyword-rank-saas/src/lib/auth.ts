import "server-only";

import { redirect } from "next/navigation";
import bcrypt from "bcryptjs";
import type { MemberRole, Organization, User } from "@prisma/client";

import { prisma } from "./db";
import { readSession } from "./session";

export type CurrentUser = User & { org: Organization };

export async function hashPassword(plain: string): Promise<string> {
  return bcrypt.hash(plain, 12);
}

export async function verifyPassword(
  plain: string,
  hash: string,
): Promise<boolean> {
  return bcrypt.compare(plain, hash);
}

/** 세션이 있으면 사용자+조직을 돌려주고, 없으면 null */
export async function getCurrentUser(): Promise<CurrentUser | null> {
  const session = await readSession();
  if (!session) return null;

  const user = await prisma.user.findUnique({
    where: { id: session.uid },
    include: { org: true },
  });
  if (!user || !user.active) return null;
  // 토큰에 박힌 orgId 와 실제 소속이 다르면 폐기 (조직 이동/삭제 대응)
  if (user.orgId !== session.orgId) return null;
  return user;
}

/** 페이지에서 사용: 미로그인 시 로그인 페이지로 보낸다 */
export async function requireUser(): Promise<CurrentUser> {
  const user = await getCurrentUser();
  if (!user) redirect("/login");
  return user;
}

const ROLE_RANK: Record<MemberRole, number> = {
  MEMBER: 1,
  ADMIN: 2,
  OWNER: 3,
};

export function hasRole(user: { role: MemberRole }, min: MemberRole): boolean {
  return ROLE_RANK[user.role] >= ROLE_RANK[min];
}

/** 조직 슬러그 생성: 한글/공백도 안전하게 처리 */
export function toSlug(name: string): string {
  const base = name
    .toLowerCase()
    .replace(/[^a-z0-9가-힣]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 30);
  const suffix = Math.random().toString(36).slice(2, 7);
  return base ? `${base}-${suffix}` : `org-${suffix}`;
}
