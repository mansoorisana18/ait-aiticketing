import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { Box } from "@mui/material";
import LoadingSkeleton from "../components/LoadingSkeleton";
import { useAuth } from "../state/AuthContext";
import type { UserRole } from "../api/types";

export default function RoleRoute({ allowedRoles }: { allowedRoles: UserRole[] }) {
  const { auth } = useAuth();

  if (auth.isBootstrapping) {
    return (
      <Box sx={{ p: 3 }}>
        <LoadingSkeleton variant="list" count={4} />
      </Box>
    );
  }

  if (!auth.userId || !auth.token) return <Navigate to="/login" replace />;
  if (!auth.role || !allowedRoles.includes(auth.role)) return <Navigate to="/tickets" replace />;
  return <Outlet />;
}