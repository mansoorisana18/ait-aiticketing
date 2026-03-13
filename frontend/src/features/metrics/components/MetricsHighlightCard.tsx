import React from "react";
import { Box, LinearProgress, Paper, Stack, Typography } from "@mui/material";
import { alpha } from "@mui/material/styles";
import MetricInfoPopover from "./MetricInfoPopover";

type MetricInfo = {
  title: string;
  summary: string;
  interpretation: string;
  calculation: string;
};

function MiniMetric({
  label,
  value,
  info,
}: {
  label: string;
  value: string | number;
  info?: MetricInfo;
}) {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 0.9,
        borderRadius: 1.5,
        bgcolor: "rgba(255,255,255,0.78)",
        minWidth: 0,
      }}
    >
      <Stack spacing={0.2}>
        <Stack direction="row" alignItems="flex-start" justifyContent="space-between" spacing={0.6}>
          <Typography
            variant="caption"
            color="text.secondary"
            sx={{
              fontWeight: 800,
              lineHeight: 1.2,
            }}
          >
            {label}
          </Typography>

          {info && (
            <MetricInfoPopover
              title={info.title}
              summary={info.summary}
              interpretation={info.interpretation}
              calculation={info.calculation}
            />
          )}
        </Stack>

        <Typography sx={{ fontWeight: 900, mt: 0.1 }}>{value}</Typography>
      </Stack>
    </Paper>
  );
}

export default function MetricsHighlightCard({
  title,
  primaryLabel,
  primaryValue,
  primaryInfo,
  secondaryLabel,
  secondaryValue,
  secondaryInfo,
  tertiaryLabel,
  tertiaryValue,
  tertiaryInfo,
  progressValue,
  progressLabel,
}: {
  title: string;
  primaryLabel: string;
  primaryValue: string | number;
  primaryInfo?: MetricInfo;
  secondaryLabel: string;
  secondaryValue: string | number;
  secondaryInfo?: MetricInfo;
  tertiaryLabel?: string;
  tertiaryValue?: string | number;
  tertiaryInfo?: MetricInfo;
  progressValue?: number | null;
  progressLabel?: string;
}) {
  const normalizedProgress =
    progressValue == null ? null : Math.max(0, Math.min(100, progressValue));

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 1.5,
        borderRadius: 2,
        minHeight: "100%",
        background:
          "linear-gradient(180deg, rgba(255,255,255,1), rgba(245,250,252,1))",
      }}
    >
      <Stack spacing={1.1}>
        <Typography sx={{ fontWeight: 900 }}>{title}</Typography>

        <Box
          sx={{
            p: 1.1,
            borderRadius: 1.75,
            bgcolor: alpha("#023047", 0.04),
            border: "1px solid rgba(2,48,71,0.08)",
          }}
        >
          <Stack spacing={0.3}>
            <Stack direction="row" alignItems="flex-start" justifyContent="space-between" spacing={0.6}>
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                {primaryLabel}
              </Typography>

              {primaryInfo && (
                <MetricInfoPopover
                  title={primaryInfo.title}
                  summary={primaryInfo.summary}
                  interpretation={primaryInfo.interpretation}
                  calculation={primaryInfo.calculation}
                />
              )}
            </Stack>

            <Typography
              variant="h4"
              sx={{
                fontWeight: 900,
                lineHeight: 1,
                color: "text.primary",
              }}
            >
              {primaryValue}
            </Typography>
          </Stack>
        </Box>

        {normalizedProgress != null && (
          <Stack spacing={0.45}>
            {progressLabel && (
              <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 800 }}>
                {progressLabel}
              </Typography>
            )}
            <LinearProgress
              variant="determinate"
              value={normalizedProgress}
              sx={{
                height: 8,
                borderRadius: 999,
              }}
            />
          </Stack>
        )}

        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: tertiaryLabel
              ? "repeat(3, minmax(0, 1fr))"
              : "repeat(2, minmax(0, 1fr))",
            gap: 0.8,
          }}
        >
          <MiniMetric
            label={secondaryLabel}
            value={secondaryValue}
            info={secondaryInfo}
          />

          {tertiaryLabel && (
            <MiniMetric
              label={tertiaryLabel}
              value={tertiaryValue ?? "—"}
              info={tertiaryInfo}
            />
          )}

          {!tertiaryLabel && <Box />}
        </Box>
      </Stack>
    </Paper>
  );
}