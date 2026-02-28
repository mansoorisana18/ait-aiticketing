export function statusChipSx(status?: string) {
  const s = (status || "").toUpperCase();

  // Adjust names if your backend uses different status strings
  switch (s) {
    case "OPEN":
      return { bgcolor: "rgba(0,180,216,0.12)", color: "#0077B6", border: "1px solid rgba(0,180,216,0.35)" };
    case "IN_PROGRESS":
      return { bgcolor: "rgba(138,86,172,0.12)", color: "#633D7D", border: "1px solid rgba(138,86,172,0.35)" };
    case "RESOLVED":
      return { bgcolor: "rgba(46,196,182,0.12)", color: "#0B6E4F", border: "1px solid rgba(46,196,182,0.35)" };
    case "CLOSED":
      return { bgcolor: "rgba(120,120,120,0.10)", color: "#4B5563", border: "1px solid rgba(120,120,120,0.30)" };
    default:
      return { bgcolor: "rgba(99,102,241,0.10)", color: "#374151", border: "1px solid rgba(99,102,241,0.25)" };
  }
}