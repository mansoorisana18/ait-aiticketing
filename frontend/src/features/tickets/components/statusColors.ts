export function statusChipSx(status?: string) {
  const s = (status || "").toUpperCase();

  //normalize a bit
  if (s.includes("DUPLICATE")) {
    return {
      bgcolor: "#F45D01",
      color: "#FFFFFF",
      border: "1px solid #AE2012",
    };
  }

  switch (s) {
    //user-facing
    case "OPEN":
      return {
        bgcolor: "#2D7DD2",
        color: "#FFFFFF",
        border: "1px solid #1B5FA7",
      };

    case "WAITING FOR YOUR INPUT":
      return {
        bgcolor: "#F45D01",
        color: "#FFFFFF",
        border: "1px solid #BB3E03",
      };

    case "IN PROGRESS":
      return {
        bgcolor: "#EEB902",
        color: "#474647",
        border: "1px solid #F45D01",
      };

    case "RESOLVED":
      return {
        bgcolor: "#97CC04",
        color: "#1F2D00",
        border: "1px solid #6A9A02",
      };

    case "CLOSED":
      return {
        bgcolor: "#474647",
        color: "#FFFFFF",
        border: "1px solid #2E2D2E",
      };

    //internal statuses for agent/admin
    case "NEW":
    case "AI_PROCESSING":
    case "READY":
      return {
        bgcolor: "#2D7DD2",
        color: "#FFFFFF",
        border: "1px solid #1B5FA7",
      };

    case "VAGUE":
      return {
        bgcolor: "#F45D01",
        color: "#FFFFFF",
        border: "1px solid #BB3E03",
      };

    case "IN_PROGRESS":
      return {
        bgcolor: "#EEB902",
        color: "#474647",
        border: "1px solid #F45D01",
      };

    case "DUPLICATE":
      return {
        bgcolor: "#F45D01",
        color: "#FFFFFF",
        border: "1px solid #AE2012",
      };

    case "RESOLVED":
      return {
        bgcolor: "#97CC04",
        color: "#1F2D00",
        border: "1px solid #6A9A02",
      };

    default:
      return {
        bgcolor: "#2D7DD2",
        color: "#FFFFFF",
        border: "1px solid #1B5FA7",
      };
  }
}