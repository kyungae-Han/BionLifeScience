"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

import { PLANS, PLAN_ORDER, formatPrice } from "@/lib/plan";

export default function PlanSwitcher({ current }: { current: string }) {
  const router = useRouter();
  const [pending, setPending] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function change(plan: string) {
    setPending(plan);
    setError(null);
    try {
      const res = await fetch("/api/org/plan", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ plan }),
      });
      const json = await res.json();
      if (!res.ok || !json.ok) {
        setError(json.message ?? "요금제 변경에 실패했습니다.");
        return;
      }
      router.refresh();
    } finally {
      setPending(null);
    }
  }

  return (
    <div>
      <div className="grid gap-3 md:grid-cols-3">
        {PLAN_ORDER.map((tier) => {
          const plan = PLANS[tier];
          const isCurrent = current === tier;
          return (
            <div
              key={tier}
              className="card p-5"
              style={isCurrent ? { borderColor: "var(--accent)", borderWidth: 2 } : undefined}
            >
              <div className="flex items-center justify-between">
                <h3 className="font-bold">{plan.label}</h3>
                {isCurrent && <span className="chip">사용 중</span>}
              </div>
              <p className="mt-2 text-2xl font-bold">{formatPrice(plan.priceMonthly)}</p>
              <ul className="mt-3 space-y-1 text-xs text-ink-2">
                <li>프로젝트 {plan.maxProjects}개</li>
                <li>키워드 {plan.maxKeywords.toLocaleString("ko-KR")}개</li>
                <li>팀원 {plan.seats}명</li>
                <li>{plan.autoDailyCollect ? "매일 자동 수집" : "수동 수집만"}</li>
              </ul>
              <button
                type="button"
                className={`btn btn-sm mt-4 w-full ${isCurrent ? "" : "btn-primary"}`}
                disabled={isCurrent || pending !== null}
                onClick={() => change(tier)}
              >
                {isCurrent ? "현재 요금제" : pending === tier ? "변경 중…" : "이 요금제로 변경"}
              </button>
            </div>
          );
        })}
      </div>
      {error && (
        <p className="mt-3 text-sm" style={{ color: "var(--critical)" }}>
          {error}
        </p>
      )}
      <p className="hint">
        결제(PG) 연동 전이라 소유자가 직접 등급을 바꾸는 방식입니다. 결제 연동 시
        <code className="mx-1">/api/org/plan</code>을 결제 성공 웹훅에서 호출하도록 바꾸면
        됩니다.
      </p>
    </div>
  );
}
