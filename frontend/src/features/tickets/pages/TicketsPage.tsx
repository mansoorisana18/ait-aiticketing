import React from "react";
import { Stack, Typography } from "@mui/material";
import { useAuth } from "../../../state/AuthContext";
import { useTicketsForUser } from "../hooks";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import TicketTable from "../components/TicketTable";

export default function TicketsPage() {
  const { auth } = useAuth();
  const { data, isLoading, isError } = useTicketsForUser(Boolean(auth.token));

  if (isLoading) return <LoadingSkeleton variant="list" count={5} />;
  if (isError) return <Typography color="error">Failed to load tickets.</Typography>;
  
  return (
    <Stack spacing={2}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        My Tickets
      </Typography>

      <TicketTable tickets={data ?? []} role={auth.role ?? "USER"} />
    </Stack>
  );
}