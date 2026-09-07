"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

export default function CollectButton({
  projectId,
  keywordId,
  label = "지금 순위 확인",
}: {
  projectId?: string;
  keywordId?: string;
  label?: string;
}) {
  const router = useRouter();
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [isError, setIsError] = useState(false);

  async function run() {
    setPending(true);
    setMessage(null);
    try {
      const res = await fetch("/api/collect", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ projectId, keywordId }),
      });
      const json = await res.json();
      if (!res.ok || !json.ok) {
        setIsError(true);
        setMessage(json.message ?? "수집에 실패했습니다.");
        return;
      }
      setIsError(false);
      setMessage(`${json.data.ok}건 수집 완료${json.data.fail ? ` (${json.data.fail}건 실패)` : ""}`);
      router.refresh();
    } catch {
      setIsError(true);
      setMessage("서버에 연결하지 못했습니다.");
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      <button type="button" className="btn btn-primary" onClick={run} disabled={pending}>
        {pending ? "수집 중…" : label}
      </button>
      {message && (
        <span
          className="text-sm"
          style={{ color: isError ? "var(--critical)" : "var(--good-text)" }}
        >
          {message}
        </span>
      )}
    </div>
  );
}
