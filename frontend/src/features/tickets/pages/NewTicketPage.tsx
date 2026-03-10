import React, { useState } from "react";
import { Paper, Typography, Stack } from "@mui/material";
import { useNavigate } from "react-router-dom";

import TicketForm from "../components/TicketForm";
import GlobalSnackbar from "../../../components/GlobalSnackbar";
import { useCreateTicket } from "../hooks";
import { normalizeApiError } from "../../../api/errorNormalizer";

export default function NewTicketPage() {
  const nav = useNavigate();
  const createTicket = useCreateTicket();

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [snack, setSnack] = useState({ open: false, message: "" });

  const onSubmit = async (payload: { title: string; description: string }) => {
    setFieldErrors({});
    try {
      const created = await createTicket.mutateAsync(payload);
      nav(`/tickets/${created.ticketId}`);
    } catch (e) {
      const ne = normalizeApiError(e);
      if (ne.kind === "validation") setFieldErrors(ne.fieldErrors ?? {});
      else setSnack({ open: true, message: ne.message });
    }
  };

  return (
    <>
      <Paper variant="outlined" sx={{ p: 3, maxWidth: 720 }}>
        <Stack spacing={2}>
          <Typography variant="h5" sx={{ fontWeight: 900 }}>
            Create Ticket
          </Typography>

          <TicketForm
            onSubmit={onSubmit}
            fieldErrors={fieldErrors}
            isSubmitting={createTicket.isPending}
          />
        </Stack>
      </Paper>

      <GlobalSnackbar
        open={snack.open}
        message={snack.message}
        onClose={() => setSnack({ open: false, message: "" })}
      />
    </>
  );
}