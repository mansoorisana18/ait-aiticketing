import React from "react";
import {
  InputAdornment,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import { useAuth } from "../../../state/AuthContext";
import { useTicketsForUser } from "../hooks";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import TicketTable from "../components/TicketTable";

export default function TicketsPage() {
  const { auth } = useAuth();
  const { data, isLoading, isError } = useTicketsForUser(Boolean(auth.token));
  const [search, setSearch] = React.useState("");

  const tickets = data ?? [];

  const filteredTickets = React.useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return tickets;

    return tickets.filter((t) => {
      const haystack = [
        String(t.ticketId ?? ""),
        t.title ?? "",
        t.description ?? "",
        t.userTicketStatus ?? "",
      ]
        .join(" ")
        .toLowerCase();

      return haystack.includes(q);
    });
  }, [tickets, search]);

  if (isLoading) return <LoadingSkeleton variant="list" count={5} />;
  if (isError) return <Typography color="error">Failed to load tickets.</Typography>;

  return (
    <Stack spacing={1.25}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        My Tickets
      </Typography>

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
          placeholder="Search by ticket, title, description, or status..."
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

      <TicketTable tickets={filteredTickets} role={auth.role ?? "USER"} />
    </Stack>
  );
}