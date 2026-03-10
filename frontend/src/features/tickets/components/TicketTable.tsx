import React from "react";
import {
  Box,
  Chip,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import type { TicketResponseBean, UserRole, UserTicketResponseBean } from "../../../api/types";
import { statusChipSx } from "./statusColors";
import { priorityChipSx } from "./priorityColors";
import { formatRelative } from "../../../utils/dateTime";

type AnyTicketRow = TicketResponseBean | UserTicketResponseBean;

type Props = {
  tickets: AnyTicketRow[];
  role: UserRole;
};

function isInternal(t: AnyTicketRow): t is TicketResponseBean {
  return (t as any).status !== undefined && (t as any).userTicketStatus !== undefined;
}

export default function TicketTable({ tickets, role }: Props) {
  const nav = useNavigate();
  const showInternalCols = role === "AGENT" || role === "ADMIN";
  const colSpan = showInternalCols ? 10 : 4;

  return (
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
            <TableCell>User Status</TableCell>

            {showInternalCols && <TableCell>Internal</TableCell>}
            {showInternalCols && <TableCell>Duplicate</TableCell>}
            {showInternalCols && <TableCell>Category</TableCell>}
            {showInternalCols && <TableCell>Priority</TableCell>}
            {showInternalCols && <TableCell>AI Confidence</TableCell>}
            {showInternalCols && <TableCell>Assigned</TableCell>}

            <TableCell>Updated</TableCell>
          </TableRow>
        </TableHead>

        <TableBody>
          {tickets.map((t) => {
            const internal = isInternal(t);
            const hasClarificationInfo =
              internal &&
              ((t.vagueCount != null && t.vagueCount > 0) ||
                Boolean(t.vagueReason) ||
                Boolean(t.clarificationPrompt));

            return (
              <TableRow
                key={t.ticketId}
                hover
                onClick={() => nav(`/tickets/${t.ticketId}`)}
                sx={{
                  cursor: "pointer",
                  "&:last-child td": { borderBottom: 0 },
                }}
              >
                <TableCell>{t.ticketId}</TableCell>

                <TableCell sx={{ maxWidth: 360 }}>
                  <Typography fontWeight={800}>{t.title}</Typography>

                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{
                      mt: 0.25,
                      display: "-webkit-box",
                      WebkitLineClamp: 2,
                      WebkitBoxOrient: "vertical",
                      overflow: "hidden",
                      whiteSpace: "normal",
                    }}
                  >
                    {t.description}
                  </Typography>

                  {!internal && t.userTicketStatus?.toUpperCase() === "WAITING FOR YOUR INPUT" && (
                    <Box
                      sx={{
                        mt: 0.75,
                        px: 1,
                        py: 0.5,
                        borderRadius: 1.5,
                        bgcolor: "rgba(245, 158, 11, 0.10)",
                        border: "1px solid rgba(245, 158, 11, 0.22)",
                      }}
                    >
                      <Typography variant="caption" sx={{ fontWeight: 700, color: "#B45309" }}>
                        Clarification requested from user
                      </Typography>
                    </Box>
                  )}

                  {hasClarificationInfo && (
                    <Box
                      sx={{
                        mt: 0.75,
                        px: 1,
                        py: 0.5,
                        borderRadius: 1.5,
                        bgcolor: "rgba(2, 132, 199, 0.08)",
                        border: "1px solid rgba(2, 132, 199, 0.18)",
                      }}
                    >
                      <Typography variant="caption" sx={{ fontWeight: 700, color: "#0369A1" }}>
                        {t.userTicketStatus?.toUpperCase() === "WAITING FOR YOUR INPUT"
                          ? "Awaiting user clarification"
                          : t.vagueCount && t.vagueCount > 0
                          ? `Clarification activity recorded (${t.vagueCount})`
                          : "Ticket details updated by user"}
                      </Typography>
                    </Box>
                  )}
                </TableCell>

                <TableCell>
                  <Chip
                    label={t.userTicketStatus}
                    size="small"
                    sx={{ ...statusChipSx(t.userTicketStatus), fontWeight: 700 }}
                  />
                </TableCell>

                {showInternalCols && (
                  <TableCell>
                    {internal ? (
                      <Chip
                        label={t.status}
                        size="small"
                        sx={{ ...statusChipSx(t.status), fontWeight: 700 }}
                      />
                    ) : (
                      "—"
                    )}
                  </TableCell>
                )}

                {showInternalCols && <TableCell>{internal ? (t.duplicateState ?? "—") : "—"}</TableCell>}
                {showInternalCols && <TableCell>{internal ? (t.aiCategory ?? "—") : "—"}</TableCell>}

                {showInternalCols && (
                  <TableCell>
                    {internal && t.aiPriority ? (
                      <Chip label={t.aiPriority} size="small" sx={{...priorityChipSx(t.aiPriority)}} />
                    ) : (
                      "—"
                    )}
                  </TableCell>
                )}

                {showInternalCols && <TableCell>{internal ? (t.aiConfidence ?? "—") : "—"}</TableCell>}
                {showInternalCols && <TableCell>{internal ? (t.assignedToName ?? "Unassigned") : "—"}</TableCell>}

                <TableCell>{formatRelative(t.updatedAt ?? null)}</TableCell>
              </TableRow>
            );
          })}

          {tickets.length === 0 && (
            <TableRow>
              <TableCell colSpan={colSpan} sx={{ py: 4 }}>
                <Typography color="text.secondary">No tickets found.</Typography>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}