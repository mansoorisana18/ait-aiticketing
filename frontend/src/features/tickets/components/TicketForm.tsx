import { useState } from "react";
import {
  Box,
  Button,
  Stack,
  Typography,
} from "@mui/material";
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
      spacing={1.75}
      component="form"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit({ title, description });
      }}
    >
      <Box>
        <Typography sx={{ fontWeight: 800, mb: 0.3 }}>
          Add a title for your issue
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 0.9 }}>
          Use a short, specific title so the issue can be understood quickly.
        </Typography>

        <FormTextField
          name="title"
          label="Ticket Title"
          placeholder="Example: Unable to access my account after password reset"
          value={title}
          onChange={(e) => setTitle((e.target as HTMLInputElement).value)}
          fieldErrors={fieldErrors}
        />
      </Box>

      <Box>
        <Typography sx={{ fontWeight: 800, mb: 0.3 }}>
          Describe the issue you are experiencing
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 0.9 }}>
          Include relevant details such as order numbers, errors, or steps already tried.
        </Typography>

        <FormTextField
          name="description"
          label="Issue Description"
          placeholder="Please describe the issue in detail..."
          multiline
          minRows={5}
          value={description}
          onChange={(e) => setDescription((e.target as HTMLInputElement).value)}
          fieldErrors={fieldErrors}
        />
      </Box>

      <Stack direction="row" justifyContent="flex-end">
        <Button type="submit" variant="contained" disabled={isSubmitting}>
          {isSubmitting ? "Creating..." : "Create Ticket"}
        </Button>
      </Stack>
    </Stack>
  );
}