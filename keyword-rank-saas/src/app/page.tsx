import Link from "next/link";

import PlanCards from "@/components/PlanCards";
import ThemeToggle from "@/components/ThemeToggle";
import { getCurrentUser } from "@/lib/auth";

export const dynamic = "force-dynamic";

const FEATURES = [
  {
    title: "3개 검색엔진 동시 추적",
    body: "네이버 웹문서·블로그, 구글, 다음까지 한 번에. 키워드마다 볼 검색영역을 따로 고를 수 있습니다.",
  },
  {
    title: "매일 자동 수집",
    body: "매일 정해진 시각에 순위를 저장해 두므로, 접속하는 순간 어제와 오늘의 변화가 이미 정리돼 있습니다.",
  },
  {
    title: "순위 변동 한눈에",
    body: "상승·하락·이탈을 색과 기호로 함께 표시합니다. 30일 추이 그래프로 흐름까지 확인하세요.",
  },
  {
    title: "여러 사이트 동시 관리",
    body: "프로젝트 단위로 사이트를 나눠 관리합니다. 대행사라면 고객사별로 분리해 운영할 수 있습니다.",
  },
  {
    title: "팀으로 함께",
    body: "조직 단위로 데이터를 공유하고, 담당자별로 관리자·조회 권한을 나눠 부여합니다.",
  },
  {
    title: "CSV 내보내기",
    body: "보고서에 붙일 수 있도록 순위 데이터를 그대로 내려받습니다.",
  },
];

const STEPS = [
  { n: "1", t: "사이트 등록", d: "추적할 도메인을 프로젝트로 등록합니다." },
  { n: "2", t: "키워드 입력", d: "여러 줄로 붙여넣으면 한 번에 등록됩니다." },
  { n: "3", t: "매일 자동 확인", d: "수집 결과가 쌓이면서 추이가 만들어집니다." },
];

export default async function LandingPage() {
  const user = await getCurrentUser();

  return (
    <div className="min-h-screen">
      <header className="border-b border-line">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-5">
          <Link href="/" className="text-[17px] font-bold tracking-tight">
            랭크<span style={{ color: "var(--accent)" }}>레이더</span>
          </Link>
          <nav className="flex items-center gap-2">
            <Link href="/pricing" className="btn btn-sm">
              요금제
            </Link>
            <ThemeToggle />
            {user ? (
              <Link href="/app" className="btn btn-sm btn-primary">
                대시보드
              </Link>
            ) : (
              <>
                <Link href="/login" className="btn btn-sm">
                  로그인
                </Link>
                <Link href="/signup" className="btn btn-sm btn-primary">
                  무료로 시작
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>

      <main>
        {/* 히어로 */}
        <section className="mx-auto max-w-6xl px-5 py-20 text-center">
          <span className="chip">네이버 · 구글 · 다음</span>
          <h1 className="mx-auto mt-6 max-w-3xl text-4xl font-bold leading-tight sm:text-5xl">
            우리 사이트가 지금 몇 위인지,
            <br />
            매일 아침 정리해 드립니다
          </h1>
          <p className="mx-auto mt-5 max-w-2xl text-lg text-ink-2">
            키워드를 등록해 두면 검색 순위를 자동으로 수집해 저장합니다. 어제보다
            올랐는지 내렸는지, 어떤 키워드가 이탈했는지 한 화면에서 확인하세요.
          </p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            <Link href={user ? "/app" : "/signup"} className="btn btn-primary">
              {user ? "대시보드로 이동" : "무료로 시작하기"}
            </Link>
            <Link href="/pricing" className="btn">
              요금제 보기
            </Link>
          </div>
          <p className="mt-4 text-sm text-muted">
            신용카드 없이 시작 · 무료 플랜 키워드 10개
          </p>
        </section>

        {/* 기능 */}
        <section className="mx-auto max-w-6xl px-5 pb-20">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {FEATURES.map((f) => (
              <div key={f.title} className="card p-6">
                <h3 className="font-bold">{f.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-ink-2">{f.body}</p>
              </div>
            ))}
          </div>
        </section>

        {/* 사용 흐름 */}
        <section className="border-y border-line bg-surface">
          <div className="mx-auto max-w-6xl px-5 py-16">
            <h2 className="text-center text-2xl font-bold">3단계면 준비 끝</h2>
            <div className="mt-10 grid gap-6 md:grid-cols-3">
              {STEPS.map((s) => (
                <div key={s.n} className="text-center">
                  <div
                    className="mx-auto flex h-11 w-11 items-center justify-center rounded-full text-lg font-bold"
                    style={{ background: "var(--accent-soft)", color: "var(--accent)" }}
                  >
                    {s.n}
                  </div>
                  <h3 className="mt-4 font-bold">{s.t}</h3>
                  <p className="mt-1 text-sm text-ink-2">{s.d}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* 요금제 */}
        <section className="mx-auto max-w-6xl px-5 py-20">
          <h2 className="text-center text-2xl font-bold">요금제</h2>
          <p className="mt-2 text-center text-ink-2">
            무료로 시작하고, 키워드가 늘어나면 그때 올리세요.
          </p>
          <div className="mt-10">
            <PlanCards />
          </div>
        </section>
      </main>

      <footer className="border-t border-line">
        <div className="mx-auto flex max-w-6xl flex-col gap-2 px-5 py-8 text-sm text-muted sm:flex-row sm:items-center sm:justify-between">
          <span>© {new Date().getFullYear()} 랭크레이더</span>
          <span>검색 순위 데이터는 각 검색엔진 공식 API 기준으로 수집합니다.</span>
        </div>
      </footer>
    </div>
  );
}
