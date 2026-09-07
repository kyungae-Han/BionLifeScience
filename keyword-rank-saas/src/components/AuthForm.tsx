"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";

type Mode = "login" | "signup";

export default function AuthForm({ mode }: { mode: Mode }) {
  const router = useRouter();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setError(null);

    const form = new FormData(event.currentTarget);
    const payload = Object.fromEntries(form.entries());

    try {
      const res = await fetch(`/api/auth/${mode}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const json = await res.json();
      if (!res.ok || !json.ok) {
        setError(json.message ?? "처리에 실패했습니다.");
        return;
      }
      router.replace("/app");
      router.refresh();
    } catch {
      setError("서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.");
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-5 py-12">
      <Link href="/" className="text-center text-lg font-bold">
        랭크<span style={{ color: "var(--accent)" }}>레이더</span>
      </Link>

      <div className="card mt-6 p-7">
        <h1 className="text-xl font-bold">
          {mode === "login" ? "로그인" : "무료로 시작하기"}
        </h1>
        <p className="mt-1 text-sm text-ink-2">
          {mode === "login"
            ? "등록한 이메일로 로그인하세요."
            : "회사(팀) 계정을 만들고 키워드를 등록해 보세요."}
        </p>

        <form onSubmit={onSubmit} className="mt-6 space-y-4">
          {mode === "signup" && (
            <>
              <div>
                <label className="label" htmlFor="orgName">
                  회사 / 팀 이름
                </label>
                <input
                  id="orgName"
                  name="orgName"
                  className="field"
                  required
                  maxLength={60}
                  placeholder="예) 바이온라이프사이언스"
                />
              </div>
              <div>
                <label className="label" htmlFor="name">
                  담당자 이름
                </label>
                <input
                  id="name"
                  name="name"
                  className="field"
                  required
                  maxLength={40}
                  placeholder="홍길동"
                />
              </div>
            </>
          )}

          <div>
            <label className="label" htmlFor="email">
              이메일
            </label>
            <input
              id="email"
              name="email"
              type="email"
              className="field"
              required
              autoComplete="email"
              placeholder="you@company.com"
            />
          </div>

          <div>
            <label className="label" htmlFor="password">
              비밀번호
            </label>
            <input
              id="password"
              name="password"
              type="password"
              className="field"
              required
              autoComplete={mode === "login" ? "current-password" : "new-password"}
              placeholder={mode === "signup" ? "영문+숫자 8자 이상" : ""}
            />
          </div>

          {error && (
            <p
              role="alert"
              className="rounded-lg px-3 py-2 text-sm"
              style={{
                background: "rgba(208,59,59,0.1)",
                color: "var(--critical)",
              }}
            >
              {error}
            </p>
          )}

          <button type="submit" className="btn btn-primary w-full" disabled={pending}>
            {pending ? "처리 중…" : mode === "login" ? "로그인" : "계정 만들기"}
          </button>
        </form>

        <p className="mt-5 text-center text-sm text-ink-2">
          {mode === "login" ? (
            <>
              계정이 없으신가요?{" "}
              <Link href="/signup" className="font-semibold" style={{ color: "var(--accent)" }}>
                무료로 시작
              </Link>
            </>
          ) : (
            <>
              이미 계정이 있으신가요?{" "}
              <Link href="/login" className="font-semibold" style={{ color: "var(--accent)" }}>
                로그인
              </Link>
            </>
          )}
        </p>
      </div>
    </div>
  );
}
