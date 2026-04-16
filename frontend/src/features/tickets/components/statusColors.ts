export function statusChipSx(status?: string) {
  const s = (status || "").toUpperCase();

  if (s.includes("DUPLICATE_REVIEW")) {
    return {
      bgcolor: "#FFB703",
      color: "#5C4300",
    };
  }
  
  if (s.includes("DUPLICATE")) {
    return {
      bgcolor: "#F45D01",
      color: "#FFFFFF",
    };
  }

  switch (s) {
    //user-facing
    case "OPEN":
      return {
        bgcolor: "#2D7DD2",
        color: "#FFFFFF",        
      };

    case "WAITING FOR YOUR INPUT":
      return {
        bgcolor: "#F45D01",
        color: "#FFFFFF",        
      };

    case "IN PROGRESS":
      return {
        bgcolor: "#EEB902",
        color: "#474647",        
      };

    case "RESOLVED":
      return {
        bgcolor: "#97CC04",
        color: "#1F2D00",        
      };

    case "CLOSED":
      return {
        bgcolor: "#474647",
        color: "#FFFFFF",        
      };

    //internal statuses for agent/admin
    case "NEW":
    case "AI_PROCESSING":
    case "READY":
      return {
        bgcolor: "#2D7DD2",
        color: "#FFFFFF",      
      };

    case "VAGUE":
      return {
        bgcolor: "#F45D01",
        color: "#FFFFFF",
      };

    case "KB_SUGGESTED":
      return {
        bgcolor: "#219EBC",
        color: "#FFFFFF",
      };

    case "IN_PROGRESS":
      return {
        bgcolor: "#EEB902",
        color: "#474647",        
      };

    case "DUPLICATE":
      return {
        bgcolor: "#F45D01",
        color: "#FFFFFF",        
      };

    default:
      return {
        bgcolor: "#2D7DD2",
        color: "#FFFFFF",        
      };
  }
}