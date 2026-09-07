"use client";

import { useEffect, useState } from "react";

type Mode = "light" | "dark";

export default function ThemeToggle() {
  const [mode, setMode] = useState<Mode | null>(null);

  useEffect(() => {
    const saved = localStorage.getItem("krs-theme");
    if (saved === "dark" || saved === "light") {
      setMode(saved);
      return;
    }
    setMode(
      window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light",
    );
  }, []);

  function toggle() {
    const next: Mode = mode === "dark" ? "light" : "dark";
    setMode(next);
    document.documentElement.dataset.theme = next;
    localStorage.setItem("krs-theme", next);
  }

  return (
    <button
      type="button"
      onClick={toggle}
      className="btn btn-sm"
      aria-label={mode === "dark" ? "라이트 모드로 전환" : "다크 모드로 전환"}
      title={mode === "dark" ? "라이트 모드" : "다크 모드"}
    >
      {mode === "dark" ? "☾" : "☀"}
    </button>
  );
}
