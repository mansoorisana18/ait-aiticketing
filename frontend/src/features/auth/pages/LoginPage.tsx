import React, { useState } from "react";
import { Paper, Stack, Typography, Button, Box } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import FormTextField from "../../../components/FormTextField";
import GlobalSnackbar from "../../../components/GlobalSnackbar";
import { useLogin } from "../hooks";
import { normalizeApiError } from "../../../api/errorNormalizer";
import { useAuth } from "../../../state/AuthContext";
import logo from "../../../assets/Logo_AiT.png";
import { subscribeSessionExpired, resetSessionExpiredEvent } from "../../../state/authEvents";

function validateLogin(email: string, password: string) {
  const errors: Record<string, string> = {};

  if (!email.trim()) {
    errors.email = "Email is required";
  } else if (!/^\S+@\S+\.\S+$/.test(email.trim())) {
    errors.email = "Enter a valid email address";
  }

  if (!password.trim()) {
    errors.password = "Password is required";
  }

  return errors;
}

export default function LoginPage() {
  const nav = useNavigate();
  const { loginSuccess } = useAuth();
  const login = useLogin();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [snack, setSnack] = useState<{ 
    open: boolean; 
    message: string; 
    severity?: "success" | "error" | "warning" | "info" 
  }>({ 
    open: false, 
    message: "", 
    severity: "info" 
  });

  React.useEffect(() => {
    resetSessionExpiredEvent();

    const unsub = subscribeSessionExpired(() => {
      setSnack((prev) => 
        prev.open ? prev : {
          open: true,
          message: "Session expired. Please login again.",
          severity: "warning",
        }
      );
    });

    return unsub;
  }, []);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const clientErrors = validateLogin(email, password);
    setFieldErrors(clientErrors);
    
    if (Object.keys(clientErrors).length > 0) return;
    try {
      const resp = await login.mutateAsync({ email: email.trim(), password });

      loginSuccess({
        userId: resp.userId,
        name: resp.name,
        email: resp.email,
        role: resp.role ?? "USER",
        token: resp.token ?? null,
      });

      nav("/");
    } catch (err) {
      const ne = normalizeApiError(err);
      if (ne.kind === "validation") setFieldErrors(ne.fieldErrors ?? {});
      else setSnack({ open: true, message: ne.message, severity: "error", });
    }
  };

  return (
    <>
      <Box
        sx={{
          minHeight: "30vh",
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          mt: 4
        }}
      >
        <Box
          component="img"
          src={logo}
          alt="Logo"
          sx={{ width: 300, mb: 2, borderRadius: 2 }}
        />
        <Typography variant="h1" fontWeight="bold">
          AI- Powered Automated Ticket Management Platform
        </Typography>
        {/* <Typography variant="h4" fontWeight="bold">
          Login to Your Account
        </Typography> */}
      </Box>
      <Paper variant="outlined" sx={{ p: 3, maxWidth: 520, mx: "auto", mt: 4 }}>
        <Stack spacing={2} component="form" onSubmit={onSubmit}>
          <Typography variant="h5">Login</Typography>

          <FormTextField
            name="email"
            label="Email"
            value={email}
            onChange={(e) => {
              setEmail((e.target as HTMLInputElement).value);
              if (fieldErrors.email) {
                setFieldErrors((prev) => ({ ...prev, email: "" }));
              }
            }}
            fieldErrors={fieldErrors}
          />      

          <FormTextField
            name="password"
            label="Password"
            type="password"
            value={password}
            onChange={(e) => {
              setPassword((e.target as HTMLInputElement).value);
              if (fieldErrors.password) {
                setFieldErrors((prev) => ({ ...prev, password: "" }));
              }
            }}
            fieldErrors={fieldErrors}
          />

          <Button type="submit" variant="contained" disabled={login.isPending}>
            Sign In
          </Button>

          <Typography variant="body2">
            Don't have an account? <Link to="/register">Register</Link>
          </Typography>
        </Stack>
      </Paper>
      <GlobalSnackbar open={snack.open} message={snack.message} severity={snack.severity} onClose={() => setSnack((s) => ({ ...s, open: false }))} />
    </>
  );
}