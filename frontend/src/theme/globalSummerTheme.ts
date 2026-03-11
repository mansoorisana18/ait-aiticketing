import { createTheme, alpha } from "@mui/material/styles";

const globalSummerTheme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#023047",
      light: "#219EBC",
      dark: "#011D2E",
      contrastText: "#FFFFFF",
    },
    secondary: {
      main: "#FB8500",
      light: "#FFB703",
      dark: "#C96D00",
      contrastText: "#FFFFFF",
    },
    background: {
      default: "#F0F7FA",
      paper: "#FFFFFF",
    },
    text: {
      primary: "#023047",
      secondary: "#4A6070",
      disabled: "#94A8B3",
    },
    success: {
      main: "#15803D",
      contrastText: "#FFFFFF",
    },
    info: {
      main: "#219EBC",
      contrastText: "#FFFFFF",
    },
    warning: {
      main: "#FFB703",
      contrastText: "#023047",
    },
    error: {
      main: "#C62828",
      contrastText: "#FFFFFF",
    },
    divider: "rgba(2,48,71,0.10)",
  },

  shape: {
    borderRadius: 10,
  },

  typography: {
    fontFamily: '"DM Sans", "Inter", "Helvetica", "Arial", sans-serif',

    h1: {
      fontWeight: 800,
      color: "#023047",
      fontSize: "2.2rem",
      lineHeight: 1.18,
    },
    h2: {
      fontWeight: 800,
      color: "#023047",
      fontSize: "1.72rem",
      lineHeight: 1.2,
    },
    h3: {
      fontWeight: 800,
      color: "#023047",
      fontSize: "1.45rem",
      lineHeight: 1.22,
    },
    h4: {
      fontWeight: 800,
      color: "#023047",
      fontSize: "1.22rem",
      lineHeight: 1.24,
    },
    h5: {
      fontWeight: 700,
      color: "#023047",
      fontSize: "1.02rem",
      lineHeight: 1.24,
    },
    h6: {
      fontWeight: 700,
      color: "#023047",
      fontSize: "0.94rem",
      lineHeight: 1.24,
    },

    subtitle1: {
      fontWeight: 600,
      color: "#023047",
      fontSize: "0.88rem",
      lineHeight: 1.35,
    },
    subtitle2: {
      fontWeight: 600,
      color: "#4A6070",
      fontSize: "0.8rem",
      lineHeight: 1.3,
    },

    body1: {
      fontSize: "0.88rem",
      lineHeight: 1.45,
      color: "#023047",
    },
    body2: {
      fontSize: "0.82rem",
      lineHeight: 1.4,
      color: "#023047",
    },

    button: {
      textTransform: "none",
      fontWeight: 700,
      fontSize: "0.8rem",
      letterSpacing: "0.01em",
    },

    caption: {
      fontSize: "0.7rem",
      lineHeight: 1.25,
      color: "#4A6070",
    },

    overline: {
      fontSize: "0.66rem",
      lineHeight: 1.2,
      fontWeight: 700,
      letterSpacing: "0.04em",
    },
  },

  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: "#F0F7FA",
          color: "#023047",
        },
      },
    },

    MuiAppBar: {
      styleOverrides: {
        root: {
          background: "#FFFFFF",
          color: "#023047",
          borderBottom: "1px solid rgba(2,48,71,0.10)",
          boxShadow: "0 2px 8px rgba(2,48,71,0.07)",
        },
        colorPrimary: {
          background: "#023047",
          color: "#FFFFFF",
          borderBottom: "none",
          boxShadow: "0 2px 12px rgba(2,48,71,0.20)",
        },
      },
    },

    MuiToolbar: {
      styleOverrides: {
        root: { minHeight: 64 },
      },
    },

    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: "#011D2E",
          borderRight: "1px solid rgba(255,255,255,0.08)",
          color: "#FFFFFF",
        },
      },
    },

    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          margin: "4px 10px",
          color: "rgba(255,255,255,0.84)",
          transition: "background-color 0.18s ease, border-color 0.18s ease, color 0.18s ease",

          "& .MuiListItemIcon-root": {
            color: "rgba(255,255,255,0.72)",
          },

          "&:hover": {
            backgroundColor: alpha("#FFB703", 0.16),
            color: "#023047",
          },

          "&.Mui-selected": {
            backgroundColor: alpha("#FFB703", 0.18),
            border: "1px solid rgba(255,183,3,0.55)",
            color: "#FFFFFF",
          },
        },
      },
    },

    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundColor: "#FFFFFF",
          backgroundImage: "linear-gradient(180deg, rgba(255,255,255,1), rgba(248,252,254,1))",
          border: "1px solid rgba(2,48,71,0.08)",
          boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
        },
        elevation1: {
          boxShadow: "0 2px 10px rgba(2,48,71,0.06)",
        },
        elevation2: {
          boxShadow: "0 4px 16px rgba(2,48,71,0.08)",
        },
        elevation3: {
          boxShadow: "0 8px 28px rgba(2,48,71,0.10)",
        },
      },
    },

    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: "linear-gradient(180deg, rgba(255,255,255,1), rgba(248,252,254,1))",
          backgroundColor: "#FFFFFF",
          border: "1px solid rgba(2,48,71,0.08)",
          boxShadow: "0 4px 20px rgba(2,48,71,0.07)",
          transition: "box-shadow 0.2s ease, transform 0.2s ease",
          "&:hover": {
            boxShadow: "0 8px 32px rgba(2,48,71,0.13)",
            transform: "translateY(-1px)",
          },
        },
      },
    },

    MuiAccordion: {
      styleOverrides: {
        root: {
          backgroundImage: "linear-gradient(180deg, rgba(255,255,255,1), rgba(248,252,254,1))",
          backgroundColor: "#FFFFFF",
          border: "1px solid rgba(2,48,71,0.09)",
          boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
          borderRadius: 10,
          overflow: "hidden",
          "&:before": { display: "none" },
          "&.Mui-expanded": {
            boxShadow: "0 6px 24px rgba(2,48,71,0.10)",
            margin: 0,
          },
        },
      },
    },

    MuiAccordionSummary: {
      styleOverrides: {
        root: {
          backgroundColor: alpha("#023047", 0.03),
          borderBottom: "1px solid rgba(2,48,71,0.08)",
          minHeight: 40,
          "&.Mui-expanded": {
            minHeight: 40,
            backgroundColor: alpha("#023047", 0.05),
          },
          "& .MuiAccordionSummary-content": {
            margin: "6px 0",
          },
          "& .MuiAccordionSummary-content.Mui-expanded": {
            margin: "6px 0",
          },
        },
      },
    },

    MuiAccordionDetails: {
      styleOverrides: {
        root: {
          backgroundColor: alpha("#FFFFFF", 0.96),
          padding: 12,
        },
      },
    },

    MuiTableHead: {
      styleOverrides: {
        root: {
          "& .MuiTableCell-head": {
            backgroundColor: "#023047",
            color: "#FFFFFF",
            fontWeight: 800,
            borderBottom: "none",
          },
        },
      },
    },

    MuiTableRow: {
      styleOverrides: {
        root: {
          "&:nth-of-type(even)": {
            backgroundColor: alpha("#8ECAE6", 0.06),
          },
          "&:hover": {
            backgroundColor: alpha("#FFB703", 0.06),
          },
        },
      },
    },

    MuiTableCell: {
      styleOverrides: {
        head: {
          fontSize: "0.76rem",
          paddingTop: 8,
          paddingBottom: 8,
        },
        body: {
          color: "#023047",
          borderBottom: "1px solid rgba(2,48,71,0.07)",
          fontSize: "0.78rem",
          paddingTop: 7,
          paddingBottom: 7,
        },
      },
    },

    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: "none",
          fontSize: "0.78rem",
          minHeight: 30,
          paddingTop: 5,
          paddingBottom: 5,
          paddingLeft: 10,
          paddingRight: 10,
          "&:hover": { boxShadow: "none" },
          "&:active": { boxShadow: "none" },
        },
        containedPrimary: {
          backgroundColor: "#023047",
          color: "#FFFFFF",
          "&:hover": {
            backgroundColor: "#03506F",
          },
          "&:active": {
            backgroundColor: "#011D2E",
          },
        },
        containedSecondary: {
          backgroundColor: "#FB8500",
          color: "#FFFFFF",
          "&:hover": {
            backgroundColor: "#E07700",
          },
          "&:active": {
            backgroundColor: "#C96D00",
          },
        },
        outlinedPrimary: {
          borderColor: "rgba(2,48,71,0.30)",
          color: "#023047",
          "&:hover": {
            backgroundColor: alpha("#8ECAE6", 0.12),
            borderColor: "#219EBC",
          },
        },
        outlinedSecondary: {
          borderColor: "rgba(251,133,0,0.40)",
          color: "#FB8500",
          "&:hover": {
            backgroundColor: alpha("#FB8500", 0.06),
            borderColor: "#FB8500",
          },
        },
      },
    },

    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          backgroundColor: "#FFFFFF",
          fontSize: "0.8rem",
          "&:hover .MuiOutlinedInput-notchedOutline": {
            borderColor: "#219EBC",
          },
          "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
            borderColor: "#023047",
            borderWidth: 2,
          },
        },
        input: {
          fontSize: "0.8rem",
        },
      },
    },

    MuiInputLabel: {
      styleOverrides: {
        root: {
          color: "#5A6B75",
          fontSize: "0.78rem",
          "&.Mui-focused": { color: "#023047" },
        },
      },
    },

    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 6,
          fontWeight: 700,
          fontSize: "0.64rem",
          height: 20,
        },
        label: {
          paddingLeft: 6,
          paddingRight: 6,
        },
        colorPrimary: {
          backgroundColor: alpha("#023047", 0.10),
          color: "#023047",
        },
        colorSecondary: {
          backgroundColor: alpha("#FB8500", 0.12),
          color: "#C96D00",
        },
        colorSuccess: {
          backgroundColor: alpha("#15803D", 0.10),
          color: "#15803D",
        },
        colorError: {
          backgroundColor: alpha("#C62828", 0.10),
          color: "#C62828",
        },
        colorWarning: {
          backgroundColor: alpha("#FFB703", 0.18),
          color: "#875F00",
        },
        colorInfo: {
          backgroundColor: alpha("#219EBC", 0.12),
          color: "#0E6B84",
        },
      },
    },

    MuiDivider: {
      styleOverrides: {
        root: {
          opacity: 1,
          borderColor: "rgba(2,48,71,0.09)",
        },
      },
    },

    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          fontWeight: 600,
          fontSize: "0.78rem",  
          boxShadow: "0 2px 8px rgba(2,48,71,0.08)",
          paddingTop: 5,
          paddingBottom: 5,
        },
        filledSuccess: { backgroundColor: "#15803D", color: "#FFFFFF" },
        filledError: { backgroundColor: "#C62828", color: "#FFFFFF" },
        filledWarning: { backgroundColor: "#FFB703", color: "#023047" },
        filledInfo: { backgroundColor: "#219EBC", color: "#FFFFFF" },
      },
    },

    MuiSnackbarContent: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          backgroundColor: "#023047",
          color: "#FFFFFF",
          fontWeight: 600,
          boxShadow: "0 8px 28px rgba(2,48,71,0.30)",
          "& .MuiSnackbarContent-action": {
            color: "#FFB703",
          },
        },
      },
    },

    MuiTabs: {
      styleOverrides: {
        indicator: {
          backgroundColor: "#FB8500",
          height: 3,
          borderRadius: 3,
        },
      },
    },

    MuiTab: {
      styleOverrides: {
        root: {
          color: "#4A6070",
          fontWeight: 700,
          fontSize: "0.875rem",
          "&.Mui-selected": {
            color: "#023047",
          },
          "&:hover": {
            color: "#023047",
            backgroundColor: alpha("#8ECAE6", 0.08),
            borderRadius: "8px 8px 0 0",
          },
        },
      },
    },

    MuiBadge: {
      styleOverrides: {
        colorPrimary: {
          backgroundColor: "#FB8500",
          color: "#FFFFFF",
        },
      },
    },

    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          backgroundColor: "#023047",
          color: "#FFFFFF",
          fontSize: "0.75rem",
          fontWeight: 600,
          borderRadius: 6,
          boxShadow: "0 4px 14px rgba(2,48,71,0.20)",
        },
        arrow: {
          color: "#023047",
        },
      },
    },

    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 12,
          boxShadow: "0 20px 60px rgba(2,48,71,0.18)",
        },
      },
    },

    MuiDialogTitle: {
      styleOverrides: {
        root: {
          backgroundColor: "#023047",
          color: "#FFFFFF",
          fontWeight: 800,
          padding: "16px 24px",
        },
      },
    },

    MuiSwitch: {
      styleOverrides: {
        switchBase: {
          "&.Mui-checked": {
            color: "#FB8500",
            "& + .MuiSwitch-track": {
              backgroundColor: alpha("#FB8500", 0.55),
            },
          },
        },
      },
    },

    MuiLinearProgress: {
      styleOverrides: {
        root: {
          borderRadius: 4,
          backgroundColor: alpha("#8ECAE6", 0.25),
        },
        barColorPrimary: {
          backgroundColor: "#FB8500",
        },
      },
    },
  },
});

export default globalSummerTheme;