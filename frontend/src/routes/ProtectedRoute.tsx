import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { Box } from "@mui/material";
import LoadingSkeleton from "../components/LoadingSkeleton";
import { useAuth } from "../state/AuthContext";

export default function ProtectedRoute() {
  const { auth, isAuthReady } = useAuth();

  //don’t redirect during hydration
  if (!isAuthReady) {
    return (
      <Box sx={{ p: 3 }}>
        <LoadingSkeleton variant="list" count={5} />
      </Box>
    );
  }

  if (!auth.userId) return <Navigate to="/login" replace />;
  return <Outlet />;
}