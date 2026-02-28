import React from "react";
import { useParams } from "react-router-dom";
import {
  Box,
  Paper,
  Stack,
  Typography,
  Divider,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

import { useTicketById } from "../hooks";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { statusChipSx } from "../components/statusColors";
import { useAuth } from "../../../state/AuthContext";
import { formatDateTime } from "../../../utils/dateTime";

export default function TicketDetailsPage() {
  const { auth } = useAuth();
  const { ticketId } = useParams();
  const idNum = ticketId ? Number(ticketId) : null;

  const { data: ticket, isLoading } = useTicketById(idNum, true);

  if (isLoading) return <LoadingSkeleton variant="detail" />;
  if (!ticket) return <Typography>Ticket not found.</Typography>;

  const isUser = auth.role === "USER";
  const isAgentOrAdmin = auth.role === "AGENT" || auth.role === "ADMIN";

  return (
    <Stack spacing={2}>
      <Paper variant="outlined" sx={{ p: 3 }}>
        <Stack direction={{ xs: "column", md: "row" }} justifyContent="space-between" spacing={2}>
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 900 }}>
              {ticket.title}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Ticket #{ticket.ticketId}
            </Typography>
          </Box>

          <Chip label={ticket.status} sx={statusChipSx(ticket.status)} />
        </Stack>

        <Divider sx={{ my: 2 }} />

        <Typography sx={{ whiteSpace: "pre-wrap" }}>{ticket.description}</Typography>

        <Divider sx={{ my: 2 }} />

        <Typography variant="body2" color="text.secondary">
          Created by: {ticket.createdByName} ({ticket.createdByEmail})
        </Typography>
      </Paper>

      {/* Two-column feel using stacked accordions; internal panels hidden for USER */}
      <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems="flex-start">
        <Box sx={{ flex: 2 }}>
          <Paper variant="outlined" sx={{ p: 2 }}>
            <Typography sx={{ fontWeight: 800, mb: 1 }}>Activity</Typography>
            <Typography variant="body2" color="text.secondary">
              Comments section.... 
            </Typography>
            <Divider sx={{ my: 2 }} />
            <Typography variant="body2" color="text.secondary">
              Comment box....
            </Typography>
          </Paper>
        </Box>

        <Box sx={{ flex: 1, width: "100%" }}>
          {/* USER sees only minimal metadata panels */}
          {(
            <Accordion defaultExpanded>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography sx={{ fontWeight: 800 }}>Details</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Typography variant="body2" color="text.secondary">
                  Updated: {formatDateTime(ticket.updatedAt) || "—"}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Created: {formatDateTime(ticket.createdAt) || "—"}
                </Typography>
              </AccordionDetails>
            </Accordion>
          )}

          {/* AGENT/ADMIN internal panels */}
          {isAgentOrAdmin && (
            <>
              <Accordion defaultExpanded>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography sx={{ fontWeight: 800 }}>Assignment</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Typography variant="body2">
                    Assigned: {ticket.assignedToName ?? "Unassigned"}
                  </Typography>
                </AccordionDetails>
              </Accordion>

              <Accordion defaultExpanded>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography sx={{ fontWeight: 800 }}>Classification</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Typography variant="body2">
                    Category: {ticket.aiCategory ?? "—"}
                  </Typography>
                  <Typography variant="body2">
                    Priority: {ticket.aiPriority ?? "—"}
                  </Typography>
                </AccordionDetails>
              </Accordion>

              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography sx={{ fontWeight: 800 }}>Audit</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Typography variant="body2" color="text.secondary">
                    Audit/history panel (future).
                  </Typography>
                </AccordionDetails>
              </Accordion>
            </>
          )}
        </Box>
      </Stack>
    </Stack>
  );
}