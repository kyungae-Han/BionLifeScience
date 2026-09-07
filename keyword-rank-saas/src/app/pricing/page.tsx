import Link from "next/link";
import type { Metadata } from "next";

import PlanCards from "@/components/PlanCards";

export const metadata: Metadata = { title: "요금제" };

const FAQ = [
  {
    q: "키워드 1개는 어떻게 계산되나요?",
    a: "키워드 텍스트 1개가 1개로 계산됩니다. 같은 키워드를 네이버·구글·다음에서 함께 보더라도 키워드 수는 1개입니다.",
  },
  {
    q: "순위는 얼마나 자주 갱신되나요?",
    a: "Pro 이상은 매일 1회 자동 수집됩니다. 모든 요금제에서 '지금 확인' 버튼으로 즉시 수집할 수 있으며, 요금제별 일일 한도가 적용됩니다.",
  },
  {
    q: "요금제를 바꾸면 데이터는 어떻게 되나요?",
    a: "이미 수집된 순위 이력은 그대로 유지됩니다. 한도를 낮추는 경우 초과분 키워드는 수집만 중단되고 삭제되지 않습니다.",
  },
];

export default function PricingPage() {
  return (
    <div className="mx-auto max-w-6xl px-5 py-16">
      <Link href="/" className="text-sm text-muted hover:underline">
        ← 홈으로
      </Link>
      <h1 className="mt-6 text-3xl font-bold">요금제</h1>
      <p className="mt-2 text-ink-2">
        모든 요금제에서 네이버·구글·다음 순위를 추적할 수 있습니다.
      </p>

      <div className="mt-10">
        <PlanCards />
      </div>

      <h2 className="mt-16 text-xl font-bold">자주 묻는 질문</h2>
      <div className="mt-4 space-y-3">
        {FAQ.map((item) => (
          <div key={item.q} className="card p-5">
            <h3 className="font-semibold">{item.q}</h3>
            <p className="mt-2 text-sm leading-relaxed text-ink-2">{item.a}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
