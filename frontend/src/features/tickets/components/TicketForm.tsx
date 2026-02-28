import React, { useState } from "react";
import { Stack, Button } from "@mui/material";
import FormTextField from "../../../components/FormTextField";

export default function TicketForm({
  onSubmit,
  fieldErrors = {},
  isSubmitting = false,
}: {
  onSubmit: (payload: { title: string; description: string }) => void;
  fieldErrors?: Record<string, string>;
  isSubmitting?: boolean;
}) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  return (
    <Stack
      spacing={2}
      component="form"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit({ title, description });
      }}
    >
      <FormTextField
        name="title"
        label="Title"
        value={title}
        onChange={(e) => setTitle((e.target as HTMLInputElement).value)}
        fieldErrors={fieldErrors}
      />
      <FormTextField
        name="description"
        label="Description"
        multiline
        minRows={4}
        value={description}
        onChange={(e) => setDescription((e.target as HTMLInputElement).value)}
        fieldErrors={fieldErrors}
      />
      <Button type="submit" variant="contained" disabled={isSubmitting}>
        Create Ticket
      </Button>
    </Stack>
  );
}