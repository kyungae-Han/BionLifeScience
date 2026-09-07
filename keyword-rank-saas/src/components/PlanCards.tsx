import Link from "next/link";

import { PLANS, PLAN_ORDER, formatPrice } from "@/lib/plan";

export default function PlanCards({
  currentPlan,
  ctaHref = "/signup",
}: {
  currentPlan?: string;
  ctaHref?: string;
}) {
  return (
    <div className="grid gap-4 md:grid-cols-3">
      {PLAN_ORDER.map((tier) => {
        const plan = PLANS[tier];
        const isCurrent = currentPlan === tier;
        const featured = tier === "PRO";
        return (
          <div
            key={tier}
            className="card p-6 flex flex-col"
            style={
              featured
                ? { borderColor: "var(--accent)", borderWidth: 2 }
                : undefined
            }
          >
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold">{plan.label}</h3>
              {featured && (
                <span
                  className="chip"
                  style={{
                    background: "var(--accent-soft)",
                    borderColor: "var(--accent)",
                    color: "var(--accent)",
                  }}
                >
                  추천
                </span>
              )}
              {isCurrent && !featured && <span className="chip">사용 중</span>}
            </div>

            <p className="mt-4 text-3xl font-bold">
              {formatPrice(plan.priceMonthly)}
              {plan.priceMonthly > 0 && (
                <span className="text-sm font-medium text-muted"> / 월</span>
              )}
            </p>

            <ul className="mt-5 space-y-2 text-sm text-ink-2 flex-1">
              {plan.highlights.map((line) => (
                <li key={line} className="flex gap-2">
                  <span style={{ color: "var(--accent)" }}>✓</span>
                  <span>{line}</span>
                </li>
              ))}
            </ul>

            <Link
              href={ctaHref}
              className={`btn mt-6 w-full ${featured ? "btn-primary" : ""}`}
            >
              {isCurrent ? "현재 요금제" : plan.priceMonthly === 0 ? "무료로 시작" : "시작하기"}
            </Link>
          </div>
        );
      })}
    </div>
  );
}
