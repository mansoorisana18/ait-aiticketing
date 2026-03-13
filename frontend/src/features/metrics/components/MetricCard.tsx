import React from "react";
import { Paper, Stack, Typography } from "@mui/material";
import MetricInfoPopover from "./MetricInfoPopover";

export default function MetricCard({
  title,
  value,
  subtitle,
  summary,
  interpretation,
  calculation,
}: {
  title: string;
  value: string | number;
  subtitle?: string;
  summary: string;
  interpretation: string;
  calculation: string;
}) {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 1.4,
        borderRadius: 2,
        height: "100%",
        minWidth: 0,
      }}
    >
      <Stack spacing={0.8}>
        <Stack direction="row" alignItems="flex-start" justifyContent="space-between" spacing={0.8}>
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{
              fontWeight: 800,
              lineHeight: 1.25,
            }}
          >
            {title}
          </Typography>

          <MetricInfoPopover
            title={title}
            summary={summary}
            interpretation={interpretation}
            calculation={calculation}
          />
        </Stack>

        <Typography
          variant="h5"
          sx={{
            fontWeight: 900,
            lineHeight: 1.05,
            color: "text.primary",
          }}
        >
          {value}
        </Typography>

        {subtitle && (
          <Typography variant="caption" color="text.secondary">
            {subtitle}
          </Typography>
        )}
      </Stack>
    </Paper>
  );
}