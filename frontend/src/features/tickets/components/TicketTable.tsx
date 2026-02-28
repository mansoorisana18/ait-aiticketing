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
import type { TicketResponseBean, UserRole } from "../../../api/types";
import { statusChipSx } from "./statusColors";
import { formatDateTime } from "../../../utils/dateTime";

type Props = {
  tickets: TicketResponseBean[];
  role: UserRole;
};

export default function TicketTable({ tickets, role }: Props) {
  const nav = useNavigate();

  const showAssignmentCols = role === "AGENT" || role === "ADMIN";
  const showInternalMetaCols = role === "AGENT" || role === "ADMIN"; // category/priority later (not for USER)

  return (
    <TableContainer component={Paper} variant="outlined">
      <Table size="medium">
        <TableHead>
          <TableRow>
            <TableCell sx={{ fontWeight: 800 }}>ID</TableCell>
            <TableCell sx={{ fontWeight: 800 }}>Title</TableCell>
            <TableCell sx={{ fontWeight: 800 }}>Status</TableCell>

            {/* Internal fields (do NOT show to USER) */}
            {showInternalMetaCols && <TableCell sx={{ fontWeight: 800 }}>Category</TableCell>}
            {showInternalMetaCols && <TableCell sx={{ fontWeight: 800 }}>Priority</TableCell>}

            {showAssignmentCols && <TableCell sx={{ fontWeight: 800 }}>Assigned</TableCell>}

            <TableCell sx={{ fontWeight: 800 }}>Updated</TableCell>
            <TableCell sx={{ fontWeight: 800 }}>Requester</TableCell>
          </TableRow>
        </TableHead>

        <TableBody>
          {tickets.map((t) => (
            <TableRow
              key={t.ticketId}
              hover
              onClick={() => nav(`/tickets/${t.ticketId}`)}
              sx={{ cursor: "pointer" }}
            >
              <TableCell>{t.ticketId}</TableCell>

              <TableCell>
                <Typography fontWeight={700}>{t.title}</Typography>
                <Typography variant="body2" color="text.secondary" noWrap>
                  {t.description}
                </Typography>
              </TableCell>

              <TableCell>
                <Chip label={t.status} size="small" sx={statusChipSx(t.status)} />
              </TableCell>

              {showInternalMetaCols && (
                <TableCell>
                  {t.aiCategory ? (
                    <Typography variant="body2">{t.aiCategory}</Typography>
                  ) : (
                    <Typography variant="body2" color="text.secondary">—</Typography>
                  )}
                </TableCell>
              )}

              {showInternalMetaCols && (
                <TableCell>
                  {t.aiPriority ? (
                    <Typography variant="body2">{t.aiPriority}</Typography>
                  ) : (
                    <Typography variant="body2" color="text.secondary">—</Typography>
                  )}
                </TableCell>
              )}

              {showAssignmentCols && (
                <TableCell>
                  {t.assignedToName ? (
                    <Typography variant="body2">{t.assignedToName}</Typography>
                  ) : (
                    <Typography variant="body2" color="text.secondary">Unassigned</Typography>
                  )}
                </TableCell>
              )}

              <TableCell>
                <Typography variant="body2">{formatDateTime(t.updatedAt) || "—"}</Typography>
              </TableCell>

              <TableCell>
                <Box>
                  <Typography variant="body2">{t.createdByName}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {t.createdByEmail}
                  </Typography>
                </Box>
              </TableCell>
            </TableRow>
          ))}

          {tickets.length === 0 && (
            <TableRow>
              <TableCell colSpan={7} sx={{ py: 4 }}>
                <Typography color="text.secondary">No tickets found.</Typography>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}