import React from "react";
import { Alert, Chip, Stack, Typography } from "@mui/material";
import { useAuth } from "../../../state/AuthContext";
import { useAllTicketsAdmin } from "../../tickets/hooks";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import TicketTable from "../../tickets/components/TicketTable";
import { useAdminTicketSummary } from "../../metrics/hooks";
import MetricsSummaryStrip from "../../metrics/components/MetricsSummaryStrip";

type AdminTicketFilter = "ALL" | "DUPLICATE_REVIEW" | "DUPLICATE";

export default function AdminTicketsPage() {
  const { auth } = useAuth();
  const [filter, setFilter] = React.useState<AdminTicketFilter>("ALL");

  const ticketsQuery = useAllTicketsAdmin(Boolean(auth.token && auth.role === "ADMIN"));
  const summaryQuery = useAdminTicketSummary(Boolean(auth.token && auth.role === "ADMIN"));

  if (ticketsQuery.isLoading) return <LoadingSkeleton variant="list" count={7} />;
  if (ticketsQuery.isError) return <Typography color="error">Failed to load admin tickets.</Typography>;

  const allTickets = ticketsQuery.data ?? [];
  const filteredTickets =
    filter === "ALL"
      ? allTickets
      : filter === "DUPLICATE_REVIEW"
      ? allTickets.filter((t) => t.status === "DUPLICATE_REVIEW")
      : allTickets.filter((t) => t.status === "DUPLICATE");

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
            { label: "Duplicate Review", value: summaryQuery.data.duplicateReviewCount },
            { label: "Confirmed Duplicates", value: summaryQuery.data.duplicateCount },
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

      <Stack direction="row" spacing={0.8} useFlexGap flexWrap="wrap">
        <Chip
          label="All"
          clickable
          color={filter === "ALL" ? "primary" : "default"}
          onClick={() => setFilter("ALL")}
          sx={{
            fontWeight: 800,
            border: filter === "ALL" ? "1px solid rgba(33,158,188,0.30)" : "1px solid rgba(2,48,71,0.10)",
            bgcolor: filter === "ALL" ? "rgba(33,158,188,0.14)" : "rgba(255,255,255,0.84)",
            color: filter === "ALL" ? "#0E6B84" : "text.primary",
          }}
        />
        <Chip
          label="Duplicate Review"
          clickable
          color={filter === "DUPLICATE_REVIEW" ? "primary" : "default"}
          onClick={() => setFilter("DUPLICATE_REVIEW")}
          sx={{
            fontWeight: 800,
            border:
              filter === "DUPLICATE_REVIEW"
                ? "1px solid rgba(33,158,188,0.30)"
                : "1px solid rgba(2,48,71,0.10)",
            bgcolor:
              filter === "DUPLICATE_REVIEW"
                ? "rgba(33,158,188,0.14)"
                : "rgba(255,255,255,0.84)",
            color: filter === "DUPLICATE_REVIEW" ? "#0E6B84" : "text.primary",
          }}
        />
        <Chip
          label="Confirmed Duplicates"
          clickable
          color={filter === "DUPLICATE" ? "primary" : "default"}
          onClick={() => setFilter("DUPLICATE")}
          sx={{
            fontWeight: 800,
            border:
              filter === "DUPLICATE"
                ? "1px solid rgba(33,158,188,0.30)"
                : "1px solid rgba(2,48,71,0.10)",
            bgcolor:
              filter === "DUPLICATE"
                ? "rgba(33,158,188,0.14)"
                : "rgba(255,255,255,0.84)",
            color: filter === "DUPLICATE" ? "#0E6B84" : "text.primary",
          }}
        />
      </Stack>

      <TicketTable tickets={filteredTickets} role={auth.role ?? "ADMIN"} />
    </Stack>
  );
}