"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import type { SearchEngine } from "@prisma/client";

import { ENGINES, ENGINE_ORDER } from "@/lib/engines";

export default function KeywordForm({
  projectId,
  maxEngines,
  liveEngines,
}: {
  projectId: string;
  maxEngines: number;
  liveEngines: Record<string, boolean>;
}) {
  const router = useRouter();
  const [engines, setEngines] = useState<SearchEngine[]>(
    ENGINE_ORDER.slice(0, Math.min(2, maxEngines)),
  );
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [isError, setIsError] = useState(false);

  function toggle(engine: SearchEngine) {
    setEngines((prev) =>
      prev.includes(engine)
        ? prev.filter((e) => e !== engine)
        : prev.length >= maxEngines
          ? prev
          : [...prev, engine],
    );
  }

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const text = String(new FormData(form).get("text") ?? "");

    setPending(true);
    setMessage(null);
    try {
      const res = await fetch("/api/keywords", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ projectId, text, engines }),
      });
      const json = await res.json();
      if (!res.ok || !json.ok) {
        setIsError(true);
        setMessage(json.message ?? "등록에 실패했습니다.");
        return;
      }
      setIsError(false);
      setMessage(`${json.data.created}개 등록했습니다.`);
      form.reset();
      router.refresh();
    } finally {
      setPending(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="card p-5">
      <h3 className="font-bold">키워드 추가</h3>

      <div className="mt-4">
        <label className="label" htmlFor="text">
          키워드
        </label>
        <textarea
          id="text"
          name="text"
          className="field"
          rows={4}
          required
          maxLength={2000}
          placeholder={"실험실 소모품\n세포배양 배지\n(줄바꿈 또는 쉼표로 여러 개 입력)"}
        />
      </div>

      <div className="mt-4">
        <span className="label">검색영역 (최대 {maxEngines}개)</span>
        <div className="flex flex-wrap gap-2">
          {ENGINE_ORDER.map((engine) => {
            const selected = engines.includes(engine);
            return (
              <button
                key={engine}
                type="button"
                onClick={() => toggle(engine)}
                className="chip"
                aria-pressed={selected}
                style={
                  selected
                    ? {
                        borderColor: "var(--accent)",
                        background: "var(--accent-soft)",
                        color: "var(--accent)",
                      }
                    : undefined
                }
              >
                <span
                  aria-hidden
                  className="inline-block h-2 w-2 rounded-full"
                  style={{ background: `var(${ENGINES[engine].colorVar})` }}
                />
                {ENGINES[engine].label}
                {!liveEngines[engine] && (
                  <span className="text-[10px] text-muted">데모</span>
                )}
              </button>
            );
          })}
        </div>
        <p className="hint">
          &quot;데모&quot; 표시는 해당 검색엔진 API 키가 아직 등록되지 않았다는 뜻입니다.
          키를 넣으면 실제 순위로 바뀝니다.
        </p>
      </div>

      {message && (
        <p
          className="mt-3 text-sm"
          style={{ color: isError ? "var(--critical)" : "var(--good-text)" }}
        >
          {message}
        </p>
      )}

      <button
        type="submit"
        className="btn btn-primary mt-5"
        disabled={pending || engines.length === 0}
      >
        {pending ? "등록 중…" : "키워드 등록"}
      </button>
    </form>
  );
}
