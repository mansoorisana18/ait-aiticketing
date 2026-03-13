export function formatPercent(value?: number | null) {
  if (value == null) return "—";
  return `${value.toFixed(1)}%`;
}

export function formatSeconds(value?: number | null) {
  if (value == null) return "—";
  if (value < 60) return `${value.toFixed(1)} sec`;
  return `${(value / 60).toFixed(1)} min`;
}

export function formatConfidence(value?: number | null) {
  if (value == null) return "—";
  return `${(value * 100).toFixed(1)}%`;
}