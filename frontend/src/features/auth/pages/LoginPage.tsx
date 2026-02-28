import React, { useState } from "react";
import { Paper, Stack, Typography, Button, Box } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import FormTextField from "../../../components/FormTextField";
import GlobalSnackbar from "../../../components/GlobalSnackbar";
import { useLogin } from "../hooks";
import { normalizeApiError } from "../../../api/errorNormalizer";
import { useAuth } from "../../../state/AuthContext";
import logo from "../../../assets/Logo_AiT.png";

export default function LoginPage() {
  const nav = useNavigate();
  const { loginSuccess } = useAuth();
  const login = useLogin();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [snack, setSnack] = useState({ open: false, message: "" });

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFieldErrors({});
    try {
      const resp = await login.mutateAsync({ email, password });

      loginSuccess({
        userId: resp.userId,
        name: resp.name,
        email: resp.email,
        role: resp.role ?? "USER",
        sessionToken: resp.sessionToken ?? null,
      });

      nav("/");
    } catch (err) {
      const ne = normalizeApiError(err);
      if (ne.kind === "validation") setFieldErrors(ne.fieldErrors ?? {});
      else setSnack({ open: true, message: ne.message });
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
        <Typography variant="h2" fontWeight="bold">
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
            onChange={(e) => setEmail((e.target as HTMLInputElement).value)}
            fieldErrors={fieldErrors}
          />

          <FormTextField
            name="password"
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword((e.target as HTMLInputElement).value)}
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
      <GlobalSnackbar open={snack.open} message={snack.message} onClose={() => setSnack({ open: false, message: "" })} />
    </>
  );
}