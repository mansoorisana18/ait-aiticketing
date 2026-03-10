import React from "react";
import { Alert, Snackbar } from "@mui/material";

type Props = {
  open: boolean;
  message: string;
  severity?: "success" | "error" | "warning" | "info";
  onClose: () => void;
};

export default function GlobalSnackbar({
  open,
  message,
  severity = "info",
  onClose,
}: Props) {
  const colorMap = {
    success: "#15803D", // green
    error: "#B91C1C",   // red
    warning: "#D97706", // orange
    info: "#0369A1",    // blue
  };
  return (
    <Snackbar open={open} autoHideDuration={2400} onClose={onClose} anchorOrigin={{ vertical: "top", horizontal: "center" }} transitionDuration={{ enter: 120, exit: 100 }}>
      <Alert onClose={onClose} severity={severity} variant="filled" sx={{ width: "100%" , fontWeight: 700, color: "#023047", boxShadow: "0px 10px 24px rgba(2,48,71,0.16)", alignItems: "center"}}>
        {message}
      </Alert>
    </Snackbar>
  );
}