export function priorityChipSx(priority?: string | null) {
  const p = (priority ?? "").toUpperCase();

  const base = {
    fontWeight: 800,
    color: "#FFFFFF",
    px: 1.2,
    borderRadius: "6px",
    letterSpacing: "0.02em",
  };

  switch (p) {
    case "URGENT":
      return {
        ...base,
        bgcolor: "#a60000", // solid red
      };

    case "HIGH":
      return {
        ...base,
        bgcolor: "#fb4b00", // bright orange
      };

    case "MEDIUM":
      return {
        ...base,
        bgcolor: "#f8c850", // amber
        color: "#023047",   
      };

    case "LOW":
      return {
        ...base,
        bgcolor: "#757f93", // muted slate
      };

    default:
      return {
        ...base,
        bgcolor: "#94A3B8", // neutral fallback
      };
  }
}