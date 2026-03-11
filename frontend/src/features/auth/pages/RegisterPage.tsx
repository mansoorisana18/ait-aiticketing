import React, { useState } from "react";
import { Paper, Stack, Typography, Button, Box } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import FormTextField from "../../../components/FormTextField";
import GlobalSnackbar from "../../../components/GlobalSnackbar";
import { useRegister } from "../hooks";
import { normalizeApiError } from "../../../api/errorNormalizer";
import logo from "../../../assets/Logo_AiT.png";

function validateRegister(name: string, email: string, password: string) {
  const errors: Record<string, string> = {};

  if (!name.trim()) {
    errors.name = "Name is required";
  } else if (name.trim().length < 2) {
    errors.name = "Name must be at least 2 characters";
  }

  if (!email.trim()) {
    errors.email = "Email is required";
  } else if (!/^\S+@\S+\.\S+$/.test(email.trim())) {
    errors.email = "Enter a valid email address";
  }

  if (!password.trim()) {
    errors.password = "Password is required";
  } else if (password.trim().length < 6) {
    errors.password = "Password must be at least 6 characters";
  }

  return errors;
}

export default function RegisterPage() {
  const nav = useNavigate();
  const register = useRegister();

  const [email, setEmail] = useState("");
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [snack, setSnack] = useState<{
    open: boolean;
    message: string;
    severity?: "success" | "error" | "warning" | "info";
  }>({
    open: false,
    message: "",
    severity: "success",
  });

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const clientErrors = validateRegister(name, email, password);
    setFieldErrors(clientErrors);

    if (Object.keys(clientErrors).length > 0) return;

    try {
      await register.mutateAsync({
        email: email.trim(),
        name: name.trim(),
        password,
      });

      setSnack({
        open: true,
        message: "Registration successful. Redirecting to login...",
        severity: "success",
      });

      setTimeout(() => {
        nav("/login");
      }, 1200);
    } catch (err) {
      const ne = normalizeApiError(err);
      if (ne.kind === "validation") {
        setFieldErrors(ne.fieldErrors ?? {});
      } else {
        setSnack({
          open: true,
          message: ne.message,
          severity: "error",
        });
      }
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
          mt: 4,
        }}
      >
        <Box
          component="img"
          src={logo}
          alt="Logo"
          sx={{ width: 300, mb: 2, borderRadius: 2 }}
        />
        <Typography variant="h1" fontWeight="bold" alignSelf="center">
          AI- Powered Automated Ticket Management Platform
        </Typography>
      </Box>

      <Paper variant="outlined" sx={{ p: 3, maxWidth: 520, mx: "auto", mt: 4 }}>
        <Stack spacing={2} component="form" onSubmit={onSubmit}>
          <Typography variant="h5">Register</Typography>

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
            name="name"
            label="Name"
            value={name}
            onChange={(e) => {
              setName((e.target as HTMLInputElement).value);
              if (fieldErrors.name) {
                setFieldErrors((prev) => ({ ...prev, name: "" }));
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

          <Button type="submit" variant="contained" disabled={register.isPending}>
            Create Account
          </Button>

          <Typography variant="body2">
            Already have an account? <Link to="/login">Login</Link>
          </Typography>
        </Stack>
      </Paper>

      <GlobalSnackbar
        open={snack.open}
        message={snack.message}
        severity={snack.severity}
        onClose={() => setSnack((s) => ({ ...s, open: false }))}
      />
    </>
  );
}