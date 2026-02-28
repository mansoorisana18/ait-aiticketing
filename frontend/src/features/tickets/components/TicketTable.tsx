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

  return (
    <TableContainer component={Paper} variant="outlined">
      <Table size="medium">
        <TableHead>
          <TableRow>
            <TableCell sx={{ fontWeight: 900 }}>ID</TableCell>
            <TableCell sx={{ fontWeight: 900 }}>Title</TableCell>
            <TableCell sx={{ fontWeight: 900 }}>User Status</TableCell>

            {showInternalCols && <TableCell sx={{ fontWeight: 900 }}>Internal</TableCell>}
            {showInternalCols && <TableCell sx={{ fontWeight: 900 }}>Duplicate</TableCell>}
            {showInternalCols && <TableCell sx={{ fontWeight: 900 }}>Category</TableCell>}
            {showInternalCols && <TableCell sx={{ fontWeight: 900 }}>Priority</TableCell>}
            {showInternalCols && <TableCell sx={{ fontWeight: 900 }}>AI Confidence</TableCell>}
            {showInternalCols && <TableCell sx={{ fontWeight: 900 }}>Text Version</TableCell>}
            {showInternalCols && <TableCell sx={{ fontWeight: 900 }}>Assigned</TableCell>}

            <TableCell sx={{ fontWeight: 900 }}>Updated</TableCell>
          </TableRow>
        </TableHead>

        <TableBody>
          {tickets.map((t) => {
            const internal = isInternal(t);

            return (
              <TableRow
                key={t.ticketId}
                hover
                onClick={() => nav(`/tickets/${t.ticketId}`)}
                sx={{ cursor: "pointer" }}
              >
                <TableCell>{t.ticketId}</TableCell>

                <TableCell>
                  <Typography fontWeight={800}>{t.title}</Typography>
                  <Typography variant="body2" color="text.secondary" noWrap>
                    {t.description}
                  </Typography>
                </TableCell>

                <TableCell>
                  <Chip label={t.userTicketStatus} size="small" sx={{ ...statusChipSx(t.userTicketStatus), fontWeight: 700 }} />
                </TableCell>

                {showInternalCols && (
                  <TableCell>
                    {internal ? <Chip label={t.status} size="small" sx={{ ...statusChipSx(t.status), fontWeight: 700 }} /> : "—"}
                  </TableCell>
                )}

                {showInternalCols && (
                  <TableCell>
                    {internal ? (t.duplicateState ?? "—") : "—"}
                  </TableCell>
                )}

                {showInternalCols && <TableCell>{internal ? (t.aiCategory ?? "—") : "—"}</TableCell>}
                {showInternalCols && <TableCell>{internal ? (t.aiPriority ?? "—") : "—"}</TableCell>}

                {showInternalCols && (
                  <TableCell>
                    {internal ? (t.aiConfidence ?? "—") : "—"}
                  </TableCell>
                )}

                {showInternalCols && (
                  <TableCell>
                    {internal ? (t.currentTextVersion ?? "—") : "—"}
                  </TableCell>
                )}

                {showInternalCols && (
                  <TableCell>
                    {internal ? (t.assignedToName ?? "Unassigned") : "—"}
                  </TableCell>
                )}

                <TableCell>
                  {formatRelative(t.updatedAt ?? null)}
                </TableCell>
              </TableRow>
            );
          })}

          {tickets.length === 0 && (
            <TableRow>
              <TableCell colSpan={8} sx={{ py: 4 }}>
                <Typography color="text.secondary">No tickets found.</Typography>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}