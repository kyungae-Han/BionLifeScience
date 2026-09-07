export default function StatTile({
  label,
  value,
  unit,
  hint,
  tone = "default",
  hero = false,
}: {
  label: string;
  value: string | number;
  unit?: string;
  hint?: string;
  tone?: "default" | "good" | "critical";
  hero?: boolean;
}) {
  const color =
    tone === "good"
      ? "var(--good-text)"
      : tone === "critical"
        ? "var(--critical)"
        : "var(--ink)";

  return (
    <div className="card p-5">
      <p className="text-sm font-semibold text-ink-2">{label}</p>
      <p
        className={hero ? "mt-2 text-5xl font-bold" : "mt-2 text-3xl font-bold"}
        style={{ color }}
      >
        {value}
        {unit && <span className="ml-1 text-base font-semibold text-muted">{unit}</span>}
      </p>
      {hint && <p className="mt-1 text-xs text-muted">{hint}</p>}
    </div>
  );
}
