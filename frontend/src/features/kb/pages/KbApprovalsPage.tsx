import {
  Alert,
  Chip,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useKbApprovals } from "../hooks";
import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { formatRelative } from "../../../utils/dateTime";

export default function KbApprovalsPage() {
  const { auth } = useAuth();
  const nav = useNavigate();
  const { data, isLoading, isError } = useKbApprovals(Boolean(auth.token && auth.role === "ADMIN"));

  if (isLoading) return <LoadingSkeleton variant="list" count={6} />;
  if (isError) return <Alert severity="error">Failed to load KB approvals.</Alert>;

  const items = data ?? [];

  return (
    <Stack spacing={1.25}>
      <Typography variant="h5" sx={{ fontWeight: 900 }}>
        KB Approvals
      </Typography>

      <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 2 }}>
        <Table size="medium">
          <TableHead>
            <TableRow
              sx={{
                "& th": {
                  fontWeight: 900,
                  color: "text.primary",
                  bgcolor: "rgba(138,86,172,0.08)",
                  borderBottom: "1px solid rgba(138,86,172,0.16)",
                },
              }}
            >
              <TableCell>ID</TableCell>
              <TableCell>Title</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Source Ticket</TableCell>
              <TableCell>Updated</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {items.map((item) => (
              <TableRow
                key={item.kbId}
                hover
                onClick={() => nav(`/kb/${item.kbId}`)}
                sx={{ cursor: "pointer" }}
              >
                <TableCell>{item.kbId}</TableCell>
                <TableCell>
                  <Typography sx={{ fontWeight: 800 }}>{item.title}</Typography>
                  {item.createdByName ? (
                    <Typography variant="body2" color="text.secondary">
                      By {item.createdByName}
                    </Typography>
                  ) : null}
                </TableCell>
                <TableCell>
                  <Chip label={item.status ?? "—"} size="small" />
                </TableCell>
                <TableCell>
                  {item.sourceTicketId ? `Ticket #${item.sourceTicketId}` : "—"}
                </TableCell>
                <TableCell>{formatRelative(item.updatedAt ?? null) ?? "—"}</TableCell>
              </TableRow>
            ))}

            {items.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} sx={{ py: 4 }}>
                  <Typography color="text.secondary">No KB approvals pending.</Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  );
}