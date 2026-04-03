export function duplicateStateChipSx(state?: string | null) {
  const s = (state ?? "").toUpperCase();

  const base = {
    fontWeight: 800,
    color: "#FFFFFF",
    px: 1.2,
    borderRadius: "6px",
    letterSpacing: "0.02em",
  };

  switch (s) {
    case "NONE":
      return {
        ...base,
        bgcolor: "#2D7DD2", //blue
      };

    case "POTENTIAL":
      return {
        ...base,
        bgcolor: "#f8c850", //amber
        color: "#023047",
      };

    case "CONFIRMED":
      return {
        ...base,
        bgcolor: "#fb4b00", //bright orange
      };

    default:
      return {
        ...base,
        bgcolor: "#94A3B8", //neutral
      };
  }
}