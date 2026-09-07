const KST_OFFSET_MS = 9 * 60 * 60 * 1000;

/** 한국 시간 기준 '날짜'만 남긴 값 (DB 의 DATE 컬럼과 1:1로 대응) */
export function kstDateOnly(base: Date = new Date()): Date {
  const kst = new Date(base.getTime() + KST_OFFSET_MS);
  return new Date(
    Date.UTC(kst.getUTCFullYear(), kst.getUTCMonth(), kst.getUTCDate()),
  );
}

export function addDays(date: Date, days: number): Date {
  return new Date(date.getTime() + days * 86_400_000);
}

/** 2026-09-07 형태 */
export function isoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

/** 9월 7일 형태 (차트 축/표에서 사용) */
export function shortDate(date: Date): string {
  const d = new Date(date);
  return `${d.getUTCMonth() + 1}.${d.getUTCDate()}`;
}

export function formatDateTime(date: Date | null | undefined): string {
  if (!date) return "-";
  const kst = new Date(date.getTime() + KST_OFFSET_MS);
  const y = kst.getUTCFullYear();
  const m = String(kst.getUTCMonth() + 1).padStart(2, "0");
  const d = String(kst.getUTCDate()).padStart(2, "0");
  const hh = String(kst.getUTCHours()).padStart(2, "0");
  const mm = String(kst.getUTCMinutes()).padStart(2, "0");
  return `${y}.${m}.${d} ${hh}:${mm}`;
}
