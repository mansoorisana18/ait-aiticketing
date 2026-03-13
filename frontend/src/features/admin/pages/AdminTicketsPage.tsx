import React from "react";
import { Alert, Stack, Typography } from "@mui/material";
import { useAuth } from "../../../state/AuthContext";
import { useAllTicketsAdmin } from "../../tickets/hooks";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import TicketTable from "../../tickets/components/TicketTable";
import { useAdminTicketSummary } from "../../metrics/hooks";
import MetricsSummaryStrip from "../../metrics/components/MetricsSummaryStrip";

export default function AdminTicketsPage() {
  const { auth } = useAuth();

  const ticketsQuery = useAllTicketsAdmin(Boolean(auth.token && auth.role === "ADMIN"));
  const summaryQuery = useAdminTicketSummary(Boolean(auth.token && auth.role === "ADMIN"));

  if (ticketsQuery.isLoading) return <LoadingSkeleton variant="list" count={7} />;
  if (ticketsQuery.isError) return <Typography color="error">Failed to load admin tickets.</Typography>;

  return (
    <Stack spacing={1.25}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        All Tickets
      </Typography>

      {!summaryQuery.isError && summaryQuery.data && (
        <MetricsSummaryStrip
          activeMetrics={[
            { label: "Total Active", value: summaryQuery.data.totalTickets },
            { label: "Vague", value: summaryQuery.data.vagueCount },
            { label: "Ready", value: summaryQuery.data.readyCount },
            { label: "In Progress", value: summaryQuery.data.inProgressCount },
            { label: "Unassigned", value: summaryQuery.data.unassignedCount },
            { label: "High Priority", value: summaryQuery.data.highPriorityCount },
            { label: "Urgent Priority", value: summaryQuery.data.urgentPriorityCount },
          ]}
          completedMetrics={[
            { label: "Resolved", value: summaryQuery.data.resolvedCount },
            { label: "Closed", value: summaryQuery.data.closedCount },
          ]}
        />
      )}

      {summaryQuery.isError && <Alert severity="warning">Failed to load summary metrics.</Alert>}

      <TicketTable tickets={ticketsQuery.data ?? []} role={auth.role ?? "ADMIN"} />
    </Stack>
  );
}