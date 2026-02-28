import React from "react";
import { TextField, type TextFieldProps } from "@mui/material";

export default function FormTextField({
  name,
  fieldErrors,
  ...props
}: TextFieldProps & { name: string; fieldErrors?: Record<string, string> }) {
  const msg = fieldErrors?.[name] ?? "";
  return <TextField name={name} error={Boolean(msg)} helperText={msg || props.helperText} fullWidth {...props} />;
}