import React from "react";
import {
  Alert,
  Button,
  InputAdornment,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import { useAuth } from "../../../state/AuthContext";
import { useAllTicketsAdmin } from "../../tickets/hooks";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import TicketTable from "../../tickets/components/TicketTable";
import { useAdminTicketSummary } from "../../metrics/hooks";
import MetricsSummaryStrip from "../../metrics/components/MetricsSummaryStrip";

type AdminQuickFilter =
  | "ALL"
  | "ACTIVE"
  | "VAGUE"
  | "READY"
  | "DUPLICATE_REVIEW"
  | "DUPLICATE"
  | "IN_PROGRESS"
  | "UNASSIGNED"
  | "HIGH"
  | "URGENT"
  | "RESOLVED"
  | "CLOSED";

function getQuickFilterLabel(filter: AdminQuickFilter) {
  switch (filter) {
    case "ACTIVE":
      return "Active Tickets";
    case "VAGUE":
      return "Vague";
    case "READY":
      return "Ready";
    case "DUPLICATE_REVIEW":
      return "Duplicate Review";
    case "DUPLICATE":
      return "Confirmed Duplicates";
    case "IN_PROGRESS":
      return "In Progress";
    case "UNASSIGNED":
      return "Unassigned";
    case "HIGH":
      return "High Priority";
    case "URGENT":
      return "Urgent Priority";
    case "RESOLVED":
      return "Resolved";
    case "CLOSED":
      return "Closed";
    default:
      return "All Tickets";
  }
}

export default function AdminTicketsPage() {
  const { auth } = useAuth();
  const [search, setSearch] = React.useState("");
  const [quickFilter, setQuickFilter] = React.useState<AdminQuickFilter>("ALL");

  const ticketsQuery = useAllTicketsAdmin(Boolean(auth.token && auth.role === "ADMIN"));
  const summaryQuery = useAdminTicketSummary(Boolean(auth.token && auth.role === "ADMIN"));

  const allTickets = ticketsQuery.data ?? [];

  const filteredTickets = React.useMemo(() => {
    let rows = allTickets;

    switch (quickFilter) {
      case "ACTIVE":
        rows = rows.filter(
          (t) => !["RESOLVED", "CLOSED", "DUPLICATE"].includes((t.status ?? "").toUpperCase())
        );
        break;
      case "VAGUE":
        rows = rows.filter((t) => (t.status ?? "").toUpperCase() === "VAGUE");
        break;
      case "READY":
        rows = rows.filter((t) => (t.status ?? "").toUpperCase() === "READY");
        break;
      case "DUPLICATE_REVIEW":
        rows = rows.filter((t) => (t.status ?? "").toUpperCase() === "DUPLICATE_REVIEW");
        break;
      case "DUPLICATE":
        rows = rows.filter((t) => (t.status ?? "").toUpperCase() === "DUPLICATE");
        break;
      case "IN_PROGRESS":
        rows = rows.filter((t) => (t.status ?? "").toUpperCase() === "IN_PROGRESS");
        break;
      case "UNASSIGNED":
        rows = rows.filter((t) => !t.assignedToName);
        break;
      case "HIGH":
        rows = rows.filter((t) => (t.aiPriority ?? "").toUpperCase() === "HIGH");
        break;
      case "URGENT":
        rows = rows.filter((t) => (t.aiPriority ?? "").toUpperCase() === "URGENT");
        break;
      case "RESOLVED":
        rows = rows.filter((t) => (t.status ?? "").toUpperCase() === "RESOLVED");
        break;
      case "CLOSED":
        rows = rows.filter((t) => (t.status ?? "").toUpperCase() === "CLOSED");
        break;
      case "ALL":
      default:
        break;
    }

    const q = search.trim().toLowerCase();
    if (!q) return rows;

    return rows.filter((t) => {
      const haystack = [
        String(t.ticketId ?? ""),
        t.title ?? "",
        t.description ?? "",
        t.userTicketStatus ?? "",
        t.status ?? "",
        t.createdByName ?? "",
        t.assignedToName ?? "",
        t.aiCategory ?? "",
        t.aiPriority ?? "",
      ]
        .join(" ")
        .toLowerCase();

      return haystack.includes(q);
    });
  }, [allTickets, quickFilter, search]);

  const activeMetricConfigs = summaryQuery.data
    ? [
        {
          label: "Total Active",
          value: summaryQuery.data.totalTickets,
          filter: "ACTIVE" as const,
        },
        {
          label: "Vague",
          value: summaryQuery.data.vagueCount,
          filter: "VAGUE" as const,
        },
        {
          label: "Ready",
          value: summaryQuery.data.readyCount,
          filter: "READY" as const,
        },
        {
          label: "Duplicate Review",
          value: summaryQuery.data.duplicateReviewCount,
          filter: "DUPLICATE_REVIEW" as const,
        },
        {
          label: "Confirmed Duplicates",
          value: summaryQuery.data.duplicateCount,
          filter: "DUPLICATE" as const,
        },
        {
          label: "In Progress",
          value: summaryQuery.data.inProgressCount,
          filter: "IN_PROGRESS" as const,
        },
        {
          label: "Unassigned",
          value: summaryQuery.data.unassignedCount,
          filter: "UNASSIGNED" as const,
        },
        {
          label: "High Priority",
          value: summaryQuery.data.highPriorityCount,
          filter: "HIGH" as const,
        },
        {
          label: "Urgent Priority",
          value: summaryQuery.data.urgentPriorityCount,
          filter: "URGENT" as const,
        },
      ]
    : [];

  const completedMetricConfigs = summaryQuery.data
    ? [
        {
          label: "Resolved",
          value: summaryQuery.data.resolvedCount,
          filter: "RESOLVED" as const,
        },
        {
          label: "Closed",
          value: summaryQuery.data.closedCount,
          filter: "CLOSED" as const,
        },
      ]
    : [];

  if (ticketsQuery.isLoading) return <LoadingSkeleton variant="list" count={7} />;
  if (ticketsQuery.isError) {
    return <Typography color="error">Failed to load admin tickets.</Typography>;
  }

  return (
    <Stack spacing={1.25}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        All Tickets
      </Typography>

      {!summaryQuery.isError && summaryQuery.data && (
        <MetricsSummaryStrip
          activeMetrics={activeMetricConfigs.map((metric) => ({
            label: metric.label,
            value: metric.value,
            onClick: () => setQuickFilter(metric.filter),
            isSelected: quickFilter === metric.filter,
          }))}
          completedMetrics={completedMetricConfigs.map((metric) => ({
            label: metric.label,
            value: metric.value,
            onClick: () => setQuickFilter(metric.filter),
            isSelected: quickFilter === metric.filter,
          }))}
        />
      )}

      {summaryQuery.isError && <Alert severity="warning">Failed to load summary metrics.</Alert>}

      <Paper
        variant="outlined"
        sx={{
          p: 1.1,
          borderRadius: 2,
          border: "1px solid rgba(2,48,71,0.10)",
          boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
        }}
      >
        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={1}
          justifyContent="space-between"
          alignItems={{ xs: "stretch", md: "center" }}
        >
          <TextField
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Keyword search across ticket details..."
            size="small"
            sx={{ width: { xs: "100%", md: 420 } }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            }}
          />

          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={0.8}
            alignItems={{ xs: "stretch", sm: "center" }}
          >
            <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 700 }}>
              Showing: {getQuickFilterLabel(quickFilter)}
            </Typography>

            {quickFilter !== "ALL" && (
              <Button variant="outlined" onClick={() => setQuickFilter("ALL")}>
                Clear Filter
              </Button>
            )}
          </Stack>
        </Stack>
      </Paper>

      {filteredTickets.length === 0 ? (
        <Alert severity="info">No tickets match the current search or quick filter.</Alert>
      ) : (
        <TicketTable tickets={filteredTickets} role={auth.role ?? "ADMIN"} />
      )}
    </Stack>
  );
}