import MemberManager from "@/components/MemberManager";
import PlanSwitcher from "@/components/PlanSwitcher";
import { requireUser } from "@/lib/auth";
import { engineStatuses } from "@/lib/collector";
import { formatDateTime } from "@/lib/date";
import { prisma } from "@/lib/db";
import { ENGINES } from "@/lib/engines";
import { manualChecksToday } from "@/lib/limits";
import { planOf } from "@/lib/plan";

export const dynamic = "force-dynamic";

export default async function SettingsPage() {
  const user = await requireUser();
  const plan = planOf(user.org.plan);

  const [members, keywordCount, projectCount, usedToday, lastRuns] = await Promise.all([
    prisma.user.findMany({
      where: { orgId: user.orgId },
      orderBy: [{ role: "asc" }, { createdAt: "asc" }],
      select: { id: true, name: true, email: true, role: true, lastLoginAt: true },
    }),
    prisma.keyword.count({ where: { project: { orgId: user.orgId } } }),
    prisma.project.count({ where: { orgId: user.orgId } }),
    manualChecksToday(user.orgId),
    prisma.collectRun.findMany({
      where: { orgId: user.orgId },
      orderBy: { startedAt: "desc" },
      take: 5,
    }),
  ]);

  const engines = engineStatuses();

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold">설정</h1>
        <p className="mt-1 text-sm text-ink-2">
          {user.org.name} · 요금제 {plan.label}
        </p>
      </div>

      <section>
        <h2 className="font-bold">사용량</h2>
        <div className="card mt-3 grid gap-4 p-5 sm:grid-cols-3">
          <Usage label="프로젝트" used={projectCount} total={plan.maxProjects} />
          <Usage label="키워드" used={keywordCount} total={plan.maxKeywords} />
          <Usage
            label="오늘 수동 확인"
            used={usedToday}
            total={plan.manualChecksPerDay}
          />
        </div>
      </section>

      <section>
        <h2 className="font-bold">검색엔진 연동</h2>
        <div className="card mt-3 divide-y divide-line">
          {engines.map(({ engine, live }) => (
            <div key={engine} className="flex items-center justify-between px-5 py-4">
              <div className="flex items-center gap-2">
                <span
                  aria-hidden
                  className="inline-block h-2.5 w-2.5 rounded-full"
                  style={{ background: `var(${ENGINES[engine].colorVar})` }}
                />
                <span className="font-semibold">{ENGINES[engine].label}</span>
              </div>
              <span
                className="chip"
                style={
                  live
                    ? { color: "var(--good-text)", borderColor: "var(--good)" }
                    : undefined
                }
              >
                {live ? "● 실제 API 연동됨" : "○ 데모 데이터"}
              </span>
            </div>
          ))}
        </div>
        <p className="hint">
          데모 상태인 검색엔진은 환경변수에 API 키를 넣으면 실제 순위로 전환됩니다.
          (네이버: NAVER_CLIENT_ID/SECRET · 구글: GOOGLE_API_KEY/GOOGLE_SEARCH_ENGINE_ID ·
          다음: KAKAO_REST_API_KEY)
        </p>
      </section>

      {user.role === "OWNER" && (
        <section>
          <h2 className="font-bold">요금제</h2>
          <div className="mt-3">
            <PlanSwitcher current={user.org.plan} />
          </div>
        </section>
      )}

      <section>
        <h2 className="font-bold">팀원</h2>
        <div className="card mt-3">
          <MemberManager
            canManage={user.role !== "MEMBER"}
            seats={plan.seats}
            members={members.map((m) => ({
              ...m,
              lastLoginAt: m.lastLoginAt ? formatDateTime(m.lastLoginAt) : null,
            }))}
          />
        </div>
      </section>

      <section>
        <h2 className="font-bold">최근 수집 이력</h2>
        <div className="card mt-3 overflow-x-auto">
          {lastRuns.length === 0 ? (
            <p className="px-5 py-8 text-center text-sm text-muted">
              아직 수집 이력이 없습니다.
            </p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>시작</th>
                  <th>구분</th>
                  <th>성공</th>
                  <th>실패</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {lastRuns.map((run) => (
                  <tr key={run.id}>
                    <td className="num text-sm">{formatDateTime(run.startedAt)}</td>
                    <td className="text-sm">
                      {run.triggeredBy.startsWith("manual") ? "수동" : "자동"}
                    </td>
                    <td className="num text-sm">{run.okCount}</td>
                    <td className="num text-sm">{run.failCount}</td>
                    <td className="text-sm">{run.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>
    </div>
  );
}

function Usage({
  label,
  used,
  total,
}: {
  label: string;
  used: number;
  total: number;
}) {
  const ratio = Math.min(100, Math.round((used / Math.max(1, total)) * 100));
  const tone =
    ratio >= 100 ? "var(--critical)" : ratio >= 80 ? "var(--warning)" : "var(--accent)";
  return (
    <div>
      <p className="text-sm font-semibold text-ink-2">{label}</p>
      <p className="num mt-1 text-xl font-bold">
        {used.toLocaleString("ko-KR")}
        <span className="ml-1 text-sm font-medium text-muted">
          / {total.toLocaleString("ko-KR")}
        </span>
      </p>
      <div
        className="mt-2 h-1.5 w-full overflow-hidden rounded-full"
        style={{ background: "var(--surface-2)" }}
        role="img"
        aria-label={`${label} ${ratio}% 사용`}
      >
        <div style={{ width: `${ratio}%`, height: "100%", background: tone }} />
      </div>
    </div>
  );
}
