export function formatPercent(value?: number | null) {
  if (value == null) return "—";
  return `${value.toFixed(1)}%`;
}

export function formatSeconds(value?: number | null) {
  if (value == null) return "—";
  if (value < 60) return `${value.toFixed(1)} sec`;
  return `${(value / 60).toFixed(1)} min`;
}

export function formatConfidence(value?: number | string | null) {
  if (value == null) return "—";

  const n = typeof value === "string" ? Number(value) : value;
  if (Number.isNaN(n)) return "—";

  if (n <= 1) return `${(n * 100).toFixed(1)}%`;
  if (n <= 100) return `${n.toFixed(1)}%`;
  return "—";
}

export function formatSimilarity(value?: number | null): string {
  if (value == null || Number.isNaN(value)) return "—";
  return value.toFixed(3);
}