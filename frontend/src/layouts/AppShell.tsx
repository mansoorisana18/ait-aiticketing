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

const drawerWidth = 200;

type NavItem = { to: string; label: string; show: boolean };

export default function AppShell({ children }: { children: React.ReactNode }) {
  const { auth, logout } = useAuth();
  const loc = useLocation();

  const isUser = auth.role === "USER";
  const isAgent = auth.role === "AGENT";
  const isAdmin = auth.role === "ADMIN";

  const links: NavItem[] = [
    { to: "/tickets", label: "My Tickets", show: isUser },
    { to: "/tickets/new", label: "Create Ticket", show: isUser },

    { to: "/agent/tickets", label: "Assigned to Me", show: isAgent },

    { to: "/admin/tickets", label: "All Tickets", show: isAdmin },
    { to: "/admin/users", label: "Manage Users", show: isAdmin },
    { to: "/admin/analytics", label: "Analytics", show: isAdmin },

    //KB
    { to: "/kb", label: "Knowledge Base", show: isAgent },
    { to: "/admin/kb", label: "Knowledge Management", show: isAdmin },
  ];

  const visible = links.filter((l) => l.show);

  const isSelected = (to: string) => {
    if (to === "/tickets") return loc.pathname === "/tickets";
    if (to === "/tickets/new") return loc.pathname === "/tickets/new";

    if (to === "/agent/tickets") return loc.pathname.startsWith("/agent/tickets");

    if (to === "/admin/tickets") return loc.pathname === "/admin/tickets";
    if (to === "/admin/users") return loc.pathname === "/admin/users";
    if (to === "/admin/analytics") return loc.pathname === "/admin/analytics";

    if (to === "/kb") {
      return (
        loc.pathname === "/kb" ||
        /^\/kb\/\d+$/.test(loc.pathname) ||
        /^\/agent\/kb\/\d+\/draft$/.test(loc.pathname)
      );
    }

    if (to === "/admin/kb") {
      return (
        loc.pathname === "/admin/kb" ||
        loc.pathname === "/admin/kb/new" ||
        loc.pathname === "/admin/kb/review" ||
        /^\/admin\/kb\/\d+\/edit$/.test(loc.pathname)
      );
    }

    return loc.pathname === to;
  };

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
      <AppBar
        position="fixed"
        color="primary"
        sx={{
          zIndex: (t) => t.zIndex.drawer + 1,
          left: 0,
          right: 0,
          width: "100%",
          margin: 0,
        }}
      >
        <Toolbar sx={{ display: "flex", justifyContent: "space-between" }}>
          <Box sx={{ display: "flex", alignItems: "center", gap: 1.25 }}>
            <img src={logo} alt="AiT" style={{ width: 94, height: 34, borderRadius: 6 }} />

            <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: 0.2, color: "#ffffff" }}>
              AI Ticketing System
            </Typography>
          </Box>

          <Box sx={{ display: "flex", gap: 1.5, alignItems: "center" }}>
            <Chip
              label={auth.role}
              size="small"
              sx={{
                bgcolor: "rgba(255,255,255,0.16)",
                color: "#fff",
                border: "1px solid rgba(255,255,255,0.24)",
                fontWeight: 700,
              }}
            />
            <Typography variant="body2" sx={{ color: "#ffffff", opacity: 0.95 }}>
              {auth.name}
            </Typography>
            <Button
              onClick={logout}
              variant="outlined"
              sx={{
                color: "#fff",
                borderColor: "rgba(255,255,255,0.45)",
                "&:hover": {
                  borderColor: "#fff",
                  backgroundColor: "rgba(255,255,255,0.10)",
                },
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
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: {
            width: drawerWidth,
            boxSizing: "border-box",
            backgroundColor: "#8ECAE6",
            color: "#023047",
            borderRight: "1px solid rgba(2,48,71,0.10)",
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
              sx={{
                mx: 1,
                my: 0.5,
                px: 2,
                borderRadius: 2,
                color: "#023047",

                "& .MuiListItemText-primary": {
                  fontWeight: 700,
                  color: "inherit",
                },

                // Hover state
                "&:hover": {
                  backgroundColor: "rgba(33,158,188,0.16)",
                },

                // Selected state
                "&.Mui-selected": {
                  backgroundColor: "rgba(255,255,255,0.70)",
                  border: "2px solid #023047",
                  color: "#023047",
                  boxShadow: "0 2px 6px rgba(2,48,71,0.15)",
                },

                "&.Mui-selected .MuiListItemText-primary": {
                  color: "#023047",
                },
              }}
            >
              <ListItemText primary={l.label} />
            </ListItemButton>
          ))}
        </List>
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          bgcolor: "background.default",
          minHeight: "100vh",
        }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
}