import { useState } from "react";
import { Alert, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import { alpha } from "@mui/material/styles";
import type { UserTicketResponseBean } from "../../../api/types";

export default function VagueClarificationPanel({
  ticket,
  onSubmit,
  isSubmitting,
}: {
  ticket: UserTicketResponseBean;
  onSubmit: (body: { title?: string; clarificationAnswer: string }) => Promise<void>;
  isSubmitting: boolean;
}) {
  const [title, setTitle] = useState(ticket.title ?? "");
  const [clarificationAnswer, setClarificationAnswer] = useState("");

  const submit = async () => {
    if (!clarificationAnswer.trim()) return;
    await onSubmit({
      title: title.trim() === ticket.title ? undefined : title.trim(),
      clarificationAnswer: clarificationAnswer.trim(),
    });
    setClarificationAnswer("");
  };

  return (
    <Paper variant="outlined" sx={(theme) => ({ p: 2, borderRadius: 2, border: `2px solid ${alpha(theme.palette.secondary.dark, 0.12)}` })}>
      <Stack spacing={1.5}>
        <Typography sx={{ fontWeight: 1000 }}>Clarification Needed</Typography>

        {ticket.vagueReason && (
          <Alert severity="warning">
            {ticket.vagueReason}
          </Alert>
        )}

        {ticket.clarificationPrompt && (
          <Typography variant="body2">
            <strong>Required:</strong> {ticket.clarificationPrompt}
          </Typography>
        )}

        <TextField
          label="Update ticket title (Optional)"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />

        <TextField
          label="Your clarification"
          placeholder= "Provide the required information to clarify the issue and help us resolve your ticket faster."
          value={clarificationAnswer}
          onChange={(e) => setClarificationAnswer(e.target.value)}
          multiline
          minRows={4}
        />

        <Button
          variant="contained"
          onClick={submit}
          disabled={isSubmitting || !clarificationAnswer.trim()}
        >
          {isSubmitting ? "Submitting..." : "Submit Clarification"}
        </Button>
      </Stack>
    </Paper>
  );
}