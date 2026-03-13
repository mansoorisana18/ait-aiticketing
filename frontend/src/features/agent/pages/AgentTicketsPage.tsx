import React from "react";
import { Alert, Stack, Typography } from "@mui/material";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import TicketTable from "../../tickets/components/TicketTable";
import { useTicketsForAgent } from "../../tickets/hooks";
import { useAgentTicketSummary } from "../../metrics/hooks";
import MetricsSummaryStrip from "../../metrics/components/MetricsSummaryStrip";

export default function AgentTicketsPage() {
  const { auth } = useAuth();

  const ticketsQuery = useTicketsForAgent(Boolean(auth.token && auth.role === "AGENT"));
  const summaryQuery = useAgentTicketSummary(Boolean(auth.token && auth.role === "AGENT"));

  if (ticketsQuery.isLoading) return <LoadingSkeleton variant="list" count={6} />;
  if (ticketsQuery.isError) return <Typography color="error">Failed to load assigned tickets.</Typography>;

  return (
    <Stack spacing={1.25}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        Assigned to Me
      </Typography>

      {!summaryQuery.isError && summaryQuery.data && (
        <MetricsSummaryStrip
          activeMetrics={[
            { label: "Total Active", value: summaryQuery.data.totalTickets },
            { label: "Ready", value: summaryQuery.data.readyCount },
            { label: "In Progress", value: summaryQuery.data.inProgressCount },
            { label: "High Priority", value: summaryQuery.data.highPriorityCount },
            { label: "Urgent Priority", value: summaryQuery.data.urgentPriorityCount },
          ]}
          completedMetrics={[
            { label: "Resolved", value: summaryQuery.data.resolvedCount },
            { label: "Closed", value: summaryQuery.data.closedCount },
          ]}
        />
      )}

      {summaryQuery.isError && <Alert severity="warning">Failed to load queue summary.</Alert>}

      <TicketTable tickets={ticketsQuery.data ?? []} role={auth.role ?? "AGENT"} />
    </Stack>
  );
}