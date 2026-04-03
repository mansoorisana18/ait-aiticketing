import React from "react";
import { Alert, Button, Paper, Stack, TextField, Typography } from "@mui/material";

export default function DuplicateReviewPanel({
  referenceTicketId,
  suggestedPrimaryTicketId,
  suggestedPrimaryTicketTitle,
  onReferenceTicketIdChange,
  reason,
  onReasonChange,
  onConfirmDuplicate,
  onMarkNotDuplicate,
  isSubmitting,
  isError,
  isSuccess,
}: {
  referenceTicketId: number | null;
  suggestedPrimaryTicketId?: number | null;
  suggestedPrimaryTicketTitle?: string | null;
  onReferenceTicketIdChange: (value: number | null) => void;
  reason: string;
  onReasonChange: (value: string) => void;
  onConfirmDuplicate: () => Promise<void> | void;
  onMarkNotDuplicate: () => Promise<void> | void;
  isSubmitting?: boolean;
  isError?: boolean;
  isSuccess?: boolean;
}) {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 1.35,
        borderRadius: 2,
        border: "1px solid rgba(2,48,71,0.10)",
        boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
      }}
    >
      <Stack spacing={1}>
        <Stack spacing={0.3}>
          <Typography sx={{ fontWeight: 1000 }}>Duplicate Review Action</Typography>
          <Typography variant="body2" color="text.secondary">
            This ticket has been flagged as a potential duplicate. Review the AI-suggested primary
            ticket, then confirm the duplicate link or mark it as not duplicate.
          </Typography>
        </Stack>

        {suggestedPrimaryTicketId ? (
          <Paper
            variant="outlined"
            sx={{
              p: 1,
              borderRadius: 1.5,
              bgcolor: "rgba(255,255,255,0.72)",
              border: "1px solid rgba(2,48,71,0.08)",
            }}
          >
            <Stack spacing={0.25}>
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                AI-suggested primary ticket
              </Typography>
              <Typography sx={{ fontWeight: 800 }}>
                {suggestedPrimaryTicketTitle ?? "Ticket"} (Ticket #{suggestedPrimaryTicketId})
              </Typography>
            </Stack>
          </Paper>
        ) : null}

        <TextField
          label="Primary Ticket ID"
          value={referenceTicketId ?? ""}
          onChange={(e) =>
            onReferenceTicketIdChange(e.target.value === "" ? null : Number(e.target.value))
          }
          size="small"
          inputProps={{
            inputMode: "numeric",
            pattern: "[0-9]*",
          }}
          helperText="Prefilled with the AI-suggested primary ticket. Update only if needed."
        />

        <TextField
          label="Reason (optional)"
          value={reason}
          onChange={(e) => onReasonChange(e.target.value)}
          multiline
          minRows={2}
          size="small"
        />

        <Stack direction={{ xs: "column", sm: "row" }} spacing={0.8}>
          <Button
            variant="contained"
            onClick={onConfirmDuplicate}
            disabled={isSubmitting || referenceTicketId == null}
          >
            Confirm Duplicate
          </Button>

          <Button
            variant="outlined"
            color="inherit"
            onClick={onMarkNotDuplicate}
            disabled={isSubmitting}
          >
            Mark as Not Duplicate
          </Button>
        </Stack>

        {isError && <Alert severity="error">Failed to apply duplicate review action.</Alert>}
        {isSuccess && <Alert severity="success">Duplicate review action applied.</Alert>}
      </Stack>
    </Paper>
  );
}