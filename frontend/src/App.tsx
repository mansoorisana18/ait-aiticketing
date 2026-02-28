import React from "react";
import { useLocation } from "react-router-dom";
import AppRoutes from "./routes/AppRoutes";
import AppShell from "./layouts/AppShell";
import { useAuth } from "./state/AuthContext";

function App() {
  const { auth } = useAuth();
    const loc = useLocation();
    const isPublic = loc.pathname.startsWith("/login") || loc.pathname.startsWith("/register");

    if (auth.userId && !isPublic) {
      return (
        <AppShell>
          <AppRoutes />
        </AppShell>
      );
    }

    return <AppRoutes />;
}

export default App