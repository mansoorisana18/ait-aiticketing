import React from "react";
import {
  AppBar,
  Box,
  Button,
  Drawer,
  List,
  ListItemButton,
  ListItemText,
  Toolbar,
  Typography,
  Chip,
} from "@mui/material";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../state/AuthContext";
import logo from "../assets/Logo_AiT.png";

const drawerWidth = 260;

type NavItem = { to: string; label: string; show: boolean };

export default function AppShell({ children }: { children: React.ReactNode }) {
  const { auth, logout } = useAuth();
  const loc = useLocation();

  const isUser = auth.role === "USER";
  const isAgent = auth.role === "AGENT";
  const isAdmin = auth.role === "ADMIN";

  // Sidebar links per role (KB included but screens not built yet)
  const links: NavItem[] = [
    // Tickets
    { to: "/tickets", label: "My Tickets", show: isUser },
    { to: "/tickets/new", label: "Create Ticket", show: isUser },

    // Agent routes
    { to: "/agent/tickets", label: "Assigned to Me", show: isAgent },

    // Admin routes
    { to: "/admin/tickets", label: "All Tickets", show: isAdmin },
    { to: "/admin/users", label: "Manage Users", show: isAdmin },
    
    // Knowledge Base placeholder routes (no pages yet)
    { to: "/kb", label: "Knowledge Base", show: true },
    { to: "/kb/my-submissions", label: "My KB Submissions", show: isAgent },
    { to: "/kb/approvals", label: "KB Approvals", show: isAdmin },
  ];

  const visible = links.filter((l) => l.show);

  //for highligting the current page in the sidebar - checks if the current path starts with the link path (to handle subpages)
  const isSelected = (to: string) => {
    if (to === "/tickets" && loc.pathname === "/tickets") return true;
    if (to === "/tickets/new" && loc.pathname === "/tickets/new") return true;
    if (to === "/agent/tickets" && loc.pathname.startsWith("/agent")) return true;
    if (to === "/admin/tickets" && loc.pathname === "/admin/tickets") return true;
    if (to === "/admin/users" && loc.pathname === "/admin/users") return true;
    if (to.startsWith("/kb") && loc.pathname.startsWith("/kb")) return true;
    return loc.pathname === to;
  };

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar
        position="fixed"
        sx={{
          zIndex: (t) => t.zIndex.drawer + 1,
          background: "linear-gradient(90deg, rgba(230,57,155,0.95), rgba(138,86,172,0.95))",
          color: "#fff",
          boxShadow: "0px 10px 30px rgba(138,86,172,0.18)",
        }}
      >
        <Toolbar sx={{ display: "flex", justifyContent: "space-between" }}>
          {/* Left: Logo + App Name */}
          <Box sx={{ display: "flex", alignItems: "center", gap: 1.25 }}>
            <img src={logo} alt="AiT" style={{ width: 94, height: 34, borderRadius: 6 }} />
            {/* <Box
              sx={{
                width: 34,
                height: 34,
                borderRadius: 2,
                background: "rgba(255,255,255,0.22)",
                border: "1px solid rgba(255,255,255,0.35)",
                display: "grid",
                placeItems: "center",
                fontWeight: 800,
              }}
              aria-label="Logo placeholder"
            >
              A
            </Box> */}

            <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: 0.2 }}>
              AI Ticketing System
            </Typography>
          </Box>

          {/* Right of AppBar: Role + User + Logout */}
          <Box sx={{ display: "flex", gap: 1.5, alignItems: "center" }}>
            <Chip
              label={auth.role}
              size="small"
              sx={{
                bgcolor: "rgba(255,255,255,0.22)",
                color: "#fff",
                border: "1px solid rgba(255,255,255,0.30)",
                fontWeight: 700,
              }}
            />
            <Typography variant="body2" sx={{ opacity: 0.95 }}>
              {auth.name}
            </Typography>
            <Button
              onClick={logout}
              variant="outlined"
              sx={{
                color: "#fff",
                borderColor: "rgba(255,255,255,0.55)",
                "&:hover": { borderColor: "#fff", backgroundColor: "rgba(255,255,255,0.12)" },
              }}
            >
              Logout
            </Button>
          </Box>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          [`& .MuiDrawer-paper`]: {
            width: drawerWidth,
            boxSizing: "border-box",
            bgcolor: "background.paper",
            borderRight: "1px solid rgba(138,86,172,0.12)",
          },
        }}
      >
        <Toolbar />
        <List sx={{ pt: 1 }}>
          {visible.map((l) => (
            <ListItemButton
              key={l.to}
              component={Link}
              to={l.to}
              selected={isSelected(l.to)}
              sx={{ mx: 1, my: 0.5, borderRadius: 2 }}
            >
              <ListItemText primary={l.label} />
            </ListItemButton>
          ))}
        </List>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
}