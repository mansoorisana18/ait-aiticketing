import React from "react";
import { Divider, Paper, Stack, Typography } from "@mui/material";
import { formatDateTime } from "../../../utils/dateTime";
import type { TicketTextVersionResponseBean } from "../../../api/types";

export default function TicketTextHistoryPanel({
  versions,
}: {
  versions: TicketTextVersionResponseBean[];
}) {
  const sorted = [...versions].sort((a, b) => (b.versionNo ?? 0) - (a.versionNo ?? 0));

  return (
    <Stack spacing={0.9}>
      {sorted.length === 0 && (
        <Typography color="text.secondary">No version history available.</Typography>
      )}

      {sorted.map((v) => (
        <Paper
          key={v.versionId}
          variant="outlined"
          sx={{
            p: 1.1,
            borderRadius: 1.75,
            border: "1px solid rgba(2,48,71,0.10)",
            boxShadow: "0 2px 8px rgba(2,48,71,0.04)",
          }}
        >
          <Stack spacing={0.65}>
            <Stack
              direction={{ xs: "column", sm: "row" }}
              justifyContent="space-between"
              alignItems={{ xs: "flex-start", sm: "center" }}
              spacing={0.4}
            >
              <Typography sx={{ fontWeight: 900 }}>Version #{v.versionNo}</Typography>

              <Typography variant="body2" color="text.secondary">
                {formatDateTime(v.createdAt)}
              </Typography>
            </Stack>

            <Divider />

            <Typography variant="body2" sx={{ fontWeight: 900 }}>
              Title
            </Typography>
            <Typography variant="body2">{v.title}</Typography>

            <Typography variant="body2" sx={{ fontWeight: 900 }}>
              Description
            </Typography>
            <Typography variant="body2" sx={{ whiteSpace: "pre-wrap", lineHeight: 1.55 }}>
              {v.description}
            </Typography>
          </Stack>
        </Paper>
      ))}
    </Stack>
  );
}