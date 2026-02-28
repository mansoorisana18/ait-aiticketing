import React from "react";
import { Alert, Paper, Stack, Typography } from "@mui/material";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import TicketTable from "../../tickets/components/TicketTable";
import { useTicketsForAgent } from "../../tickets/hooks";

export default function AgentTicketsPage() {
  const { auth } = useAuth();
  const { data, isLoading } = useTicketsForAgent(Boolean(auth.userId));

  if (isLoading) return <LoadingSkeleton variant="list" count={6} />;


  return (
    <Stack spacing={2}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        Assigned to Me
      </Typography>

      {/* Filters (status)*/}
      <TicketTable tickets={data ?? []} role={auth.role} />
    </Stack>
  );
}