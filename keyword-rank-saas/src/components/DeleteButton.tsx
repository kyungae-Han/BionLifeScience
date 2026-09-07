"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

export default function DeleteButton({
  endpoint,
  confirmMessage,
  label = "삭제",
  redirectTo,
}: {
  endpoint: string;
  confirmMessage: string;
  label?: string;
  redirectTo?: string;
}) {
  const router = useRouter();
  const [pending, setPending] = useState(false);

  async function onClick() {
    if (!window.confirm(confirmMessage)) return;
    setPending(true);
    try {
      const res = await fetch(endpoint, { method: "DELETE" });
      const json = await res.json().catch(() => ({}));
      if (!res.ok || !json.ok) {
        window.alert(json.message ?? "삭제에 실패했습니다.");
        return;
      }
      if (redirectTo) router.push(redirectTo);
      router.refresh();
    } finally {
      setPending(false);
    }
  }

  return (
    <button type="button" className="btn btn-sm btn-danger" onClick={onClick} disabled={pending}>
      {pending ? "삭제 중…" : label}
    </button>
  );
}
