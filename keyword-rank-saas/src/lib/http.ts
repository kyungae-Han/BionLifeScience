import { NextResponse } from "next/server";
import { ZodError } from "zod";

export function ok<T>(data: T, init?: number) {
  return NextResponse.json({ ok: true, data }, { status: init ?? 200 });
}

export function fail(message: string, status = 400, extra?: unknown) {
  return NextResponse.json({ ok: false, message, extra }, { status });
}

/** API 라우트 공통 에러 변환 */
export function toErrorResponse(error: unknown) {
  if (error instanceof ZodError) {
    const first = error.issues[0];
    return fail(first?.message ?? "입력값이 올바르지 않습니다.", 422);
  }
  if (error instanceof Error) {
    if (error.message.startsWith("AUTH:")) {
      return fail(error.message.replace("AUTH:", ""), 401);
    }
    if (error.message.startsWith("FORBIDDEN:")) {
      return fail(error.message.replace("FORBIDDEN:", ""), 403);
    }
    if (error.message.startsWith("NOTFOUND:")) {
      return fail(error.message.replace("NOTFOUND:", ""), 404);
    }
    if (error.message.startsWith("LIMIT:")) {
      return fail(error.message.replace("LIMIT:", ""), 402);
    }
  }
  console.error("[api] unhandled error", error);
  return fail("처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.", 500);
}
