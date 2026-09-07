"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

export default function ProjectForm({ disabled }: { disabled?: boolean }) {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    setPending(true);
    setError(null);
    try {
      const res = await fetch("/api/projects", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(Object.fromEntries(new FormData(form).entries())),
      });
      const json = await res.json();
      if (!res.ok || !json.ok) {
        setError(json.message ?? "등록에 실패했습니다.");
        return;
      }
      form.reset();
      setOpen(false);
      router.refresh();
    } finally {
      setPending(false);
    }
  }

  if (!open) {
    return (
      <button
        type="button"
        className="btn btn-primary"
        onClick={() => setOpen(true)}
        disabled={disabled}
        title={disabled ? "요금제의 프로젝트 한도를 모두 사용했습니다." : undefined}
      >
        + 프로젝트 추가
      </button>
    );
  }

  return (
    <form onSubmit={onSubmit} className="card w-full p-5">
      <h3 className="font-bold">새 프로젝트</h3>
      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        <div>
          <label className="label" htmlFor="name">
            프로젝트 이름
          </label>
          <input id="name" name="name" className="field" required maxLength={60} />
        </div>
        <div>
          <label className="label" htmlFor="targetDomain">
            추적 대상 도메인
          </label>
          <input
            id="targetDomain"
            name="targetDomain"
            className="field"
            required
            placeholder="example.com"
          />
          <p className="hint">https:// 없이 도메인만 입력해도 됩니다.</p>
        </div>
      </div>
      <div className="mt-4">
        <label className="label" htmlFor="memo">
          메모 (선택)
        </label>
        <input id="memo" name="memo" className="field" maxLength={300} />
      </div>

      {error && (
        <p className="mt-3 text-sm" style={{ color: "var(--critical)" }}>
          {error}
        </p>
      )}

      <div className="mt-5 flex gap-2">
        <button type="submit" className="btn btn-primary" disabled={pending}>
          {pending ? "저장 중…" : "저장"}
        </button>
        <button type="button" className="btn" onClick={() => setOpen(false)}>
          취소
        </button>
      </div>
    </form>
  );
}
