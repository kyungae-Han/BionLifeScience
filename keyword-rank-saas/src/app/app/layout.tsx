import Link from "next/link";

import ThemeToggle from "@/components/ThemeToggle";
import { requireUser } from "@/lib/auth";
import { planOf } from "@/lib/plan";

export const dynamic = "force-dynamic";

const NAV = [
  { href: "/app", label: "대시보드" },
  { href: "/app/projects", label: "프로젝트" },
  { href: "/app/settings", label: "설정" },
];

export default async function AppLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const user = await requireUser();
  const plan = planOf(user.org.plan);

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-line bg-surface">
        <div className="mx-auto flex h-16 max-w-6xl items-center gap-4 px-5">
          <Link href="/app" className="font-bold tracking-tight">
            랭크<span style={{ color: "var(--accent)" }}>레이더</span>
          </Link>

          <nav className="hidden gap-1 sm:flex">
            {NAV.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="rounded-lg px-3 py-2 text-sm font-semibold text-ink-2 hover:bg-surface-2"
              >
                {item.label}
              </Link>
            ))}
          </nav>

          <div className="ml-auto flex items-center gap-2">
            <span className="chip hidden sm:inline-flex">
              {user.org.name} · {plan.label}
            </span>
            <ThemeToggle />
            <form action="/api/auth/logout" method="post">
              <button type="submit" className="btn btn-sm">
                로그아웃
              </button>
            </form>
          </div>
        </div>

        <nav className="flex gap-1 border-t border-line px-3 py-2 sm:hidden">
          {NAV.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="rounded-lg px-3 py-1.5 text-sm font-semibold text-ink-2"
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </header>

      <main className="mx-auto max-w-6xl px-5 py-8">{children}</main>
    </div>
  );
}
