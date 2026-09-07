import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: {
    default: "랭크레이더 · 검색어 순위 추적 SaaS",
    template: "%s | 랭크레이더",
  },
  description:
    "네이버·구글·다음 검색 순위를 매일 자동으로 추적하고, 순위 변동을 한 화면에서 확인하세요.",
};

// 다크모드 깜빡임(FOUC) 방지: 렌더 전에 저장된 테마를 html 에 반영
const themeScript = `
(function () {
  try {
    var t = localStorage.getItem("krs-theme");
    if (t === "dark" || t === "light") document.documentElement.dataset.theme = t;
  } catch (e) {}
})();
`;

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="ko" suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeScript }} />
      </head>
      <body className="min-h-screen antialiased">{children}</body>
    </html>
  );
}
