import { createTheme } from "@mui/material/styles";

const cyberLightTheme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#E6399B",
      light: "#FF6DBE",
      dark: "#B02674",
      contrastText: "#FFFFFF",
    },
    secondary: {
      main: "#8A56AC",
      light: "#A374D5",
      dark: "#633D7D",
      contrastText: "#FFFFFF",
    },
    background: {
      default: "#FFFFFF",
      paper: "#FBF9FE",
    },
    text: {
      primary: "#1A0B2E",
      secondary: "#6E5D7E",
    },
    info: {
      main: "#00B4D8",
    },
    divider: "rgba(138, 86, 172, 0.10)",
  },

  shape: {
    borderRadius: 10,
  },

  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    button: {
      textTransform: "none",
      fontWeight: 600,
    },
  },

  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          // light background with subtle cyber glow (very mild)
          backgroundImage:
            "radial-gradient(900px 600px at 15% 10%, rgba(230,57,155,0.08), transparent 60%)," +
            "radial-gradient(900px 600px at 85% 15%, rgba(138,86,172,0.07), transparent 60%)," +
            "radial-gradient(900px 600px at 70% 85%, rgba(0,180,216,0.06), transparent 60%)",
          backgroundAttachment: "fixed",
        },
      },
    },

    MuiAppBar: {
      styleOverrides: {
        root: {
          // light appbar with neon accent line
          background: "#FFFFFF",
          color: "#1A0B2E",
          borderBottom: "1px solid rgba(138, 86, 172, 0.12)",
          boxShadow: "0px 2px 12px rgba(138, 86, 172, 0.06)",
        },
      },
    },

    MuiToolbar: {
      styleOverrides: {
        root: {
          minHeight: 64,
        },
      },
    },

    MuiDrawer: {
      styleOverrides: {
        paper: {
          background: "#FBF9FE",
          borderRight: "1px solid rgba(138, 86, 172, 0.12)",
        },
      },
    },

    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          marginLeft: 8,
          marginRight: 8,
          marginTop: 4,
          marginBottom: 4,
        },
      },
    },

    MuiPaper: {
      styleOverrides: {
        root: {
          // cards/panels: crisp, light, slightly tinted
          border: "1px solid rgba(138, 86, 172, 0.10)",
          boxShadow: "0px 6px 24px rgba(138, 86, 172, 0.06)",
          backgroundImage:
            "linear-gradient(180deg, rgba(255,255,255,0.90), rgba(251,249,254,0.90))",
        },
      },
    },

    MuiCard: {
      styleOverrides: {
        root: {
          border: "1px solid rgba(138, 86, 172, 0.10)",
          boxShadow: "0px 6px 24px rgba(138, 86, 172, 0.06)",
        },
      },
    },

    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          fontWeight: 600,
        },
        containedPrimary: {
          boxShadow: "0px 8px 18px rgba(230,57,155,0.18)",
        },
        containedSecondary: {
          boxShadow: "0px 8px 18px rgba(138,86,172,0.18)",
        },
      },
    },

    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          backgroundColor: "#FFFFFF",
        },
      },
    },

    MuiInputLabel: {
      styleOverrides: {
        root: {
          color: "#6E5D7E",
        },
      },
    },

    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 10,
        },
      },
    },

    MuiDivider: {
      styleOverrides: {
        root: {
          opacity: 1,
        },
      },
    },
  },
});

export default cyberLightTheme;