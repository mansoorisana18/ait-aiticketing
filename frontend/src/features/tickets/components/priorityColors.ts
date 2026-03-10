export function priorityChipSx(priority?: string | null) {
  const p = (priority ?? "").toUpperCase();

  switch (p) {
    case "URGENT":
      return {
        fontWeight: 700,
        bgcolor: "rgba(220, 38, 38, 0.12)",
        color: "#B91C1C",
        border: "1px solid rgba(220, 38, 38, 0.28)",
      };
    case "HIGH":
      return {
        fontWeight: 700,
        bgcolor: "rgba(245, 158, 11, 0.14)",
        color: "#B45309",
        border: "1px solid rgba(245, 158, 11, 0.28)",
      };
    case "MEDIUM":
      return {
        fontWeight: 700,
        bgcolor: "rgba(2, 132, 199, 0.12)",
        color: "#0369A1",
        border: "1px solid rgba(2, 132, 199, 0.26)",
      };
    case "LOW":
      return {
        fontWeight: 700,
        bgcolor: "rgba(22, 163, 74, 0.12)",
        color: "#15803D",
        border: "1px solid rgba(22, 163, 74, 0.26)",
      };
    default:
      return {
        fontWeight: 700,
      };
  }
}