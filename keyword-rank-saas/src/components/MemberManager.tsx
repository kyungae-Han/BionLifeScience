"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

type Member = {
  id: string;
  name: string;
  email: string;
  role: string;
  lastLoginAt: string | null;
};

const ROLE_LABEL: Record<string, string> = {
  OWNER: "소유자",
  ADMIN: "관리자",
  MEMBER: "조회 전용",
};

export default function MemberManager({
  members,
  canManage,
  seats,
}: {
  members: Member[];
  canManage: boolean;
  seats: number;
}) {
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
      const res = await fetch("/api/org/members", {
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

  async function remove(id: string, name: string) {
    if (!window.confirm(`'${name}' 계정을 삭제할까요?`)) return;
    const res = await fetch(`/api/org/members/${id}`, { method: "DELETE" });
    const json = await res.json().catch(() => ({}));
    if (!res.ok || !json.ok) {
      window.alert(json.message ?? "삭제에 실패했습니다.");
      return;
    }
    router.refresh();
  }

  return (
    <div>
      <div className="overflow-x-auto">
        <table className="table">
          <thead>
            <tr>
              <th>이름</th>
              <th>이메일</th>
              <th>권한</th>
              <th>마지막 로그인</th>
              {canManage && <th />}
            </tr>
          </thead>
          <tbody>
            {members.map((m) => (
              <tr key={m.id}>
                <td className="font-semibold">{m.name}</td>
                <td className="text-ink-2">{m.email}</td>
                <td>
                  <span className="chip">{ROLE_LABEL[m.role] ?? m.role}</span>
                </td>
                <td className="num text-sm text-muted">{m.lastLoginAt ?? "-"}</td>
                {canManage && (
                  <td className="text-right">
                    {m.role !== "OWNER" && (
                      <button
                        type="button"
                        className="btn btn-sm btn-danger"
                        onClick={() => remove(m.id, m.name)}
                      >
                        삭제
                      </button>
                    )}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {canManage && (
        <div className="px-5 py-4">
          {open ? (
            <form onSubmit={onSubmit} className="grid gap-3 sm:grid-cols-2">
              <div>
                <label className="label" htmlFor="member-name">
                  이름
                </label>
                <input id="member-name" name="name" className="field" required maxLength={40} />
              </div>
              <div>
                <label className="label" htmlFor="member-email">
                  이메일
                </label>
                <input
                  id="member-email"
                  name="email"
                  type="email"
                  className="field"
                  required
                />
              </div>
              <div>
                <label className="label" htmlFor="member-password">
                  임시 비밀번호
                </label>
                <input
                  id="member-password"
                  name="password"
                  className="field"
                  required
                  minLength={8}
                  placeholder="8자 이상"
                />
              </div>
              <div>
                <label className="label" htmlFor="member-role">
                  권한
                </label>
                <select id="member-role" name="role" className="field" defaultValue="MEMBER">
                  <option value="ADMIN">관리자 (프로젝트·키워드 관리)</option>
                  <option value="MEMBER">조회 전용</option>
                </select>
              </div>

              {error && (
                <p className="sm:col-span-2 text-sm" style={{ color: "var(--critical)" }}>
                  {error}
                </p>
              )}

              <div className="sm:col-span-2 flex gap-2">
                <button type="submit" className="btn btn-primary" disabled={pending}>
                  {pending ? "등록 중…" : "팀원 등록"}
                </button>
                <button type="button" className="btn" onClick={() => setOpen(false)}>
                  취소
                </button>
              </div>
            </form>
          ) : (
            <button
              type="button"
              className="btn"
              onClick={() => setOpen(true)}
              disabled={members.length >= seats}
              title={members.length >= seats ? "요금제의 팀원 한도를 모두 사용했습니다." : undefined}
            >
              + 팀원 추가 ({members.length}/{seats})
            </button>
          )}
        </div>
      )}
    </div>
  );
}
