import React from "react";
import { Stack, Typography } from "@mui/material";
import { useAuth } from "../../../state/AuthContext";
import { useAllTicketsAdmin } from "../../tickets/hooks";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import TicketTable from "../../tickets/components/TicketTable";

export default function AdminTicketsPage() {
  const { auth } = useAuth();
  const { data, isLoading, isError } = useAllTicketsAdmin(Boolean(auth.token && auth.role === "ADMIN"));

  if (isLoading) return <LoadingSkeleton variant="list" count={7} />;
  if (isError) return <Typography color="error">Failed to load admin tickets.</Typography>;

  return (
    <Stack spacing={2}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        All Tickets (Admin)
      </Typography>

      <TicketTable tickets={data ?? []} role={auth.role ?? "ADMIN"} />
    </Stack>
  );
}