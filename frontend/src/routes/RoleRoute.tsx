import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { Box } from "@mui/material";
import LoadingSkeleton from "../components/LoadingSkeleton";
import { useAuth } from "../state/AuthContext";
import type { UserRole } from "../api/types";

export default function RoleRoute({ allowedRoles }: { allowedRoles: UserRole[] }) {
  const { auth, isAuthReady } = useAuth();

  if (!isAuthReady) {
    return (
      <Box sx={{ p: 3 }}>
        <LoadingSkeleton variant="list" count={4} />
      </Box>
    );
  }

  if (!auth.userId) return <Navigate to="/login" replace />;
  if (!allowedRoles.includes(auth.role)) return <Navigate to="/tickets" replace />;
  return <Outlet />;
}