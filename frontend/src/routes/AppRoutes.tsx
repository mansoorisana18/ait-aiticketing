import { Routes, Route, Navigate } from "react-router-dom";

import ProtectedRoute from "./ProtectedRoute";
import RoleRoute from "./RoleRoute";

import LoginPage from "../features/auth/pages/LoginPage";
import RegisterPage from "../features/auth/pages/RegisterPage";

import TicketsPage from "../features/tickets/pages/TicketsPage";
import NewTicketPage from "../features/tickets/pages/NewTicketPage";
import TicketDetailsPage from "../features/tickets/pages/TicketDetailsPage";

import AdminTicketsPage from "../features/admin/pages/AdminTicketsPage";
import AgentTicketsPage from "../features/agent/pages/AgentTicketsPage";

import { useAuth } from "../state/AuthContext";
import LoadingSkeleton from "../components/LoadingSkeleton";
import ManageUsersPage from "../features/admin/pages/ManageUsersPage";
import AnalyticsPage from "../features/metrics/pages/AnalyticsPage";

import KbListPage from "../features/kb/pages/KbListPage";
import MyKbSubmissionsPage from "../features/kb/pages/MyKbSubmissionsPage";
import KbApprovalsPage from "../features/kb/pages/KbApprovalsPage";
import KbDetailsPage from "../features/kb/pages/KbDetailsPage";

function HomeRedirect() {
  const { auth } = useAuth();

  if (auth.isBootstrapping) return <LoadingSkeleton variant="list" count={4} />;
  if (!auth.userId || !auth.token) return <Navigate to="/login" replace />;

  //Logged in: role-based landing
  if (auth.role === "ADMIN") return <Navigate to="/admin/tickets" replace />;
  if (auth.role === "AGENT") return <Navigate to="/agent/tickets" replace />;
  return <Navigate to="/tickets" replace />; //USER default
}

export default function AppRoutes() {
  return (
    <Routes>
      {/* Role-aware home */}
      <Route path="/" element={<HomeRedirect />} />

      {/* Public */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Private */}
      <Route element={<ProtectedRoute />}>
        <Route path="/tickets" element={<TicketsPage />} />
        <Route path="/tickets/new" element={<NewTicketPage />} />
        <Route path="/tickets/:ticketId" element={<TicketDetailsPage />} />

        <Route path="/kb" element={<KbListPage />} />
        <Route path="/kb/:kbId" element={<KbDetailsPage />} />

        {/* AGENT-only */}
        <Route element={<RoleRoute allowedRoles={["AGENT"]} />}>
          <Route path="/agent/tickets" element={<AgentTicketsPage />} />
          <Route path="/kb/my-submissions" element={<MyKbSubmissionsPage />} />
        </Route>

        {/* Admin-only */}
        <Route element={<RoleRoute allowedRoles={["ADMIN"]} />}>
          <Route path="/admin/tickets" element={<AdminTicketsPage />} />
          <Route path="/admin/users" element={<ManageUsersPage />} />
          <Route path="/admin/analytics" element={<AnalyticsPage />} />
           <Route path="/kb/approvals" element={<KbApprovalsPage />} />
        </Route>
      </Route>

      {/* Unknown path → go to role-aware home */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}