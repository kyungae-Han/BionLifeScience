import { z } from "zod";

const ENGINE_VALUES = ["NAVER_WEB", "NAVER_BLOG", "GOOGLE", "DAUM_WEB"] as const;

export const engineSchema = z.enum(ENGINE_VALUES);

export const signupSchema = z.object({
  orgName: z.string().trim().min(2, "회사(팀) 이름을 2자 이상 입력해 주세요.").max(60),
  name: z.string().trim().min(1, "이름을 입력해 주세요.").max(40),
  email: z.string().trim().toLowerCase().email("이메일 형식이 올바르지 않습니다."),
  password: z
    .string()
    .min(8, "비밀번호는 8자 이상이어야 합니다.")
    .max(72, "비밀번호는 72자를 넘을 수 없습니다.")
    .regex(/[A-Za-z]/, "비밀번호에 영문을 포함해 주세요.")
    .regex(/[0-9]/, "비밀번호에 숫자를 포함해 주세요."),
});

export const loginSchema = z.object({
  email: z.string().trim().toLowerCase().email("이메일 형식이 올바르지 않습니다."),
  password: z.string().min(1, "비밀번호를 입력해 주세요."),
});

const domainRegex = /^[a-z0-9-]+(\.[a-z0-9-]+)+$/;

export const projectSchema = z.object({
  name: z.string().trim().min(1, "프로젝트 이름을 입력해 주세요.").max(60),
  targetDomain: z
    .string()
    .trim()
    .toLowerCase()
    .transform((v) => v.replace(/^https?:\/\//, "").replace(/^www\./, "").split("/")[0])
    .refine((v) => domainRegex.test(v), "도메인 형식이 올바르지 않습니다. 예) example.com"),
  memo: z.string().trim().max(300).optional().or(z.literal("")),
});

export const keywordCreateSchema = z.object({
  projectId: z.string().min(1),
  /** 줄바꿈 또는 쉼표로 여러 개 입력 가능 */
  text: z.string().trim().min(1, "키워드를 입력해 주세요.").max(2000),
  engines: z.array(engineSchema).min(1, "검색영역을 최소 1개 선택해 주세요."),
});

export const keywordUpdateSchema = z.object({
  engines: z.array(engineSchema).min(1).optional(),
  active: z.boolean().optional(),
});

export const memberSchema = z.object({
  name: z.string().trim().min(1, "이름을 입력해 주세요.").max(40),
  email: z.string().trim().toLowerCase().email("이메일 형식이 올바르지 않습니다."),
  password: z.string().min(8, "비밀번호는 8자 이상이어야 합니다.").max(72),
  role: z.enum(["ADMIN", "MEMBER"]),
});

export const planSchema = z.object({
  plan: z.enum(["FREE", "PRO", "BUSINESS"]),
});

/** 여러 줄/쉼표 입력을 키워드 배열로 정규화 (중복·공백 제거) */
export function parseKeywordList(raw: string): string[] {
  const parts = raw
    .split(/[\n,]/)
    .map((v) => v.trim())
    .filter((v) => v.length > 0 && v.length <= 60);
  return Array.from(new Set(parts));
}
