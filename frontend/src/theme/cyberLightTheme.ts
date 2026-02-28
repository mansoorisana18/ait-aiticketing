import { createTheme } from "@mui/material/styles";

const cyberLightTheme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#023047",       // deep navy
      light: "#219EBC",      // teal
      contrastText: "#FFFFFF",
    },
    secondary: {
      main: "#8ECAE6",       // soft sky
      contrastText: "#023047",
    },
    background: {
      default: "#F4FAFD",    // very light tint of palette
      paper: "#FFFFFF",
    },
    text: {
      primary: "#023047",
      secondary: "#5A6B75",
    },
    info: {
      main: "#219EBC",
      contrastText: "#FFFFFF",
    },
    warning: {
      main: "#FFB703",
    },
    error: {
      main: "#FB8500",
    },
    divider: "rgba(2,48,71,0.12)",
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
          backgroundImage:
            "radial-gradient(900px 600px at 15% 10%, rgba(142,202,230,0.20), transparent 60%)," +
            "radial-gradient(900px 600px at 85% 15%, rgba(33,158,188,0.18), transparent 60%)," +
            "radial-gradient(900px 600px at 70% 85%, rgba(255,183,3,0.12), transparent 60%)",
          backgroundAttachment: "fixed",
        },
      },
    },

    MuiAppBar: {
      styleOverrides: {
        root: {
          background: "#FFFFFF",
          color: "#023047",
          borderBottom: "1px solid rgba(2,48,71,0.10)",
          boxShadow: "0px 2px 12px rgba(2,48,71,0.08)",
        },
        colorPrimary: {
          background: "linear-gradient(90deg,#023047 0%,#219EBC 100%)",
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
          backgroundColor: "#8ECAE6",
          borderRight: "1px solid rgba(2,48,71,0.10)",
        },
      },
    },

    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          margin: "4px 8px",
        },
      },
    },

    MuiPaper: {
      styleOverrides: {
        root: {
          border: "1px solid rgba(2,48,71,0.08)",
          boxShadow: "0px 6px 24px rgba(2,48,71,0.08)",
          backgroundImage:
            "linear-gradient(180deg, rgba(255,255,255,0.95), rgba(244,250,253,0.95))",
        },
      },
    },

    MuiCard: {
      styleOverrides: {
        root: {
          border: "1px solid rgba(2,48,71,0.08)",
          boxShadow: "0px 6px 24px rgba(2,48,71,0.08)",
        },
      },
    },

    MuiButton: {
      styleOverrides: {
        containedPrimary: {
          backgroundColor: "#023047",
          color: "#fff",
          "&:hover": {
            backgroundColor: "#219EBC",
          },
        },
        containedSecondary: {
          backgroundColor: "#FFB703",
          color: "#023047",
          "&:hover": {
            backgroundColor: "#FB8500",
            color: "#fff",
          },
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
          color: "#5A6B75",
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