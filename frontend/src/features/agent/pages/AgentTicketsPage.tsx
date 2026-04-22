import React from "react";
import {
  Alert,
  InputAdornment,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import TicketTable from "../../tickets/components/TicketTable";
import { useTicketsForAgent } from "../../tickets/hooks";
import { useAgentTicketSummary } from "../../metrics/hooks";
import MetricsSummaryStrip from "../../metrics/components/MetricsSummaryStrip";

export default function AgentTicketsPage() {
  const { auth } = useAuth();
  const [search, setSearch] = React.useState("");

  const ticketsQuery = useTicketsForAgent(Boolean(auth.token && auth.role === "AGENT"));
  const summaryQuery = useAgentTicketSummary(Boolean(auth.token && auth.role === "AGENT"));

  const tickets = ticketsQuery.data ?? [];

  const filteredTickets = React.useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return tickets;

    return tickets.filter((t) => {
      const haystack = [
        String(t.ticketId ?? ""),
        t.title ?? "",
        t.description ?? "",
        t.userTicketStatus ?? "",
        t.status ?? "",
        t.createdByName ?? "",
        t.aiCategory ?? "",
        t.aiPriority ?? "",
      ]
        .join(" ")
        .toLowerCase();

      return haystack.includes(q);
    });
  }, [tickets, search]);

  if (ticketsQuery.isLoading) return <LoadingSkeleton variant="list" count={6} />;
  if (ticketsQuery.isError) {
    return <Typography color="error">Failed to load assigned tickets.</Typography>;
  }

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

      <Paper
        variant="outlined"
        sx={{
          p: 1.1,
          borderRadius: 2,
          border: "1px solid rgba(2,48,71,0.10)",
          boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
        }}
      >
        <TextField
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by ticket, title, description, requestor, category, or priority..."
          size="small"
          fullWidth
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon fontSize="small" />
              </InputAdornment>
            ),
          }}
        />
      </Paper>

      <TicketTable tickets={filteredTickets} role={auth.role ?? "AGENT"} />
    </Stack>
  );
}