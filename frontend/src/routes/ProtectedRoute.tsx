import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { Box } from "@mui/material";
import LoadingSkeleton from "../components/LoadingSkeleton";
import { useAuth } from "../state/AuthContext";

export default function ProtectedRoute() {
  const { auth } = useAuth();

  //show loader while startup /refresh is happening
  if (auth.isBootstrapping) {
    return (
      <Box sx={{ p: 3 }}>
        <LoadingSkeleton variant="list" count={5} />
      </Box>
    );
  }

  //enforcing auth afetr bootstrapping is done
  if (!auth.userId || !auth.token) return <Navigate to="/login" replace />;
  return <Outlet />;
}