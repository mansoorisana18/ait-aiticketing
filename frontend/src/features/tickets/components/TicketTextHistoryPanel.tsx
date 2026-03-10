import React from "react";
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Divider,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { formatDateTime } from "../../../utils/dateTime";
import type { TicketTextVersionResponseBean } from "../../../api/types";

export default function TicketTextHistoryPanel({
  versions,
}: {
  versions: TicketTextVersionResponseBean[];
}) {
  const sorted = [...versions].sort((a, b) => (b.versionNo ?? 0) - (a.versionNo ?? 0));

  return (
    <Accordion defaultExpanded>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography sx={{ fontWeight: 1000 }}>Text Version History</Typography>
      </AccordionSummary>

      <AccordionDetails>
        <Stack spacing={2}>
          {sorted.length === 0 && (
            <Typography color="text.secondary">No version history available.</Typography>
          )}

          {sorted.map((v) => (
            <Paper
              key={v.versionId}
              variant="outlined"
              sx={{ p: 2, borderRadius: 2, border: "1px solid rgba(138,86,172,0.12)" }}
            >
              <Stack spacing={1}>
                <Typography sx={{ fontWeight: 900 }}>
                  Version #{v.versionNo}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {formatDateTime(v.createdAt)}
                </Typography>

                <Divider />

                <Typography variant="body2" sx={{ fontWeight: 900 }}>
                  Title
                </Typography>
                <Typography variant="body2">{v.title}</Typography>

                <Typography variant="body2" sx={{ fontWeight: 900 }}>
                  Description
                </Typography>
                <Typography variant="body2" sx={{ whiteSpace: "pre-wrap" }}>
                  {v.description}
                </Typography>
              </Stack>
            </Paper>
          ))}
        </Stack>
      </AccordionDetails>
    </Accordion>
  );
}