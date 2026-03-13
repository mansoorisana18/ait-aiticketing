import React from "react";
import {
  Box,
  Divider,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { alpha } from "@mui/material/styles";

import ConfirmationNumberOutlinedIcon from "@mui/icons-material/ConfirmationNumberOutlined";
import HelpOutlineOutlinedIcon from "@mui/icons-material/HelpOutlineOutlined";
import TaskAltOutlinedIcon from "@mui/icons-material/TaskAltOutlined";
import HourglassTopOutlinedIcon from "@mui/icons-material/HourglassTopOutlined";
import DoneAllOutlinedIcon from "@mui/icons-material/DoneAllOutlined";
import PersonOffOutlinedIcon from "@mui/icons-material/PersonOffOutlined";
import PriorityHighOutlinedIcon from "@mui/icons-material/PriorityHighOutlined";
import AssignmentIndOutlinedIcon from "@mui/icons-material/AssignmentIndOutlined";
import Inventory2OutlinedIcon from "@mui/icons-material/Inventory2Outlined";

type SummaryMetric = {
  label: string;
  value: number | string;
};

function metricVisual(label: string) {
  const normalized = label.toUpperCase();

  if (normalized.includes("TOTAL ASSIGNED")) {
    return {
      icon: <AssignmentIndOutlinedIcon fontSize="small" />,
      bg: alpha("#023047", 0.08),
      border: "1px solid rgba(2,48,71,0.12)",
      iconColor: "#023047",
    };
  }

  if (normalized.includes("TOTAL ACTIVE") || normalized.includes("TOTAL TICKETS")) {
    return {
      icon: <ConfirmationNumberOutlinedIcon fontSize="small" />,
      bg: alpha("#023047", 0.08),
      border: "1px solid rgba(2,48,71,0.12)",
      iconColor: "#023047",
    };
  }

  if (normalized.includes("VAGUE")) {
    return {
      icon: <HelpOutlineOutlinedIcon fontSize="small" />,
      bg: alpha("#FFB703", 0.18),
      border: "1px solid rgba(255,183,3,0.24)",
      iconColor: "#875F00",
    };
  }

  if (normalized.includes("READY")) {
    return {
      icon: <TaskAltOutlinedIcon fontSize="small" />,
      bg: alpha("#219EBC", 0.12),
      border: "1px solid rgba(33,158,188,0.20)",
      iconColor: "#0E6B84",
    };
  }

  if (normalized.includes("IN PROGRESS")) {
    return {
      icon: <HourglassTopOutlinedIcon fontSize="small" />,
      bg: alpha("#023047", 0.08),
      border: "1px solid rgba(2,48,71,0.12)",
      iconColor: "#023047",
    };
  }

  if (normalized.includes("RESOLVED")) {
    return {
      icon: <DoneAllOutlinedIcon fontSize="small" />,
      bg: alpha("#15803D", 0.10),
      border: "1px solid rgba(21,128,61,0.20)",
      iconColor: "#15803D",
    };
  }

  if (normalized.includes("CLOSED")) {
    return {
      icon: <Inventory2OutlinedIcon fontSize="small" />,
      bg: alpha("#023047", 0.06),
      border: "1px solid rgba(2,48,71,0.10)",
      iconColor: "#4A6070",
    };
  }

  if (normalized.includes("UNASSIGNED")) {
    return {
      icon: <PersonOffOutlinedIcon fontSize="small" />,
      bg: alpha("#C62828", 0.10),
      border: "1px solid rgba(198,40,40,0.18)",
      iconColor: "#C62828",
    };
  }

  if (normalized.includes("URGENT")) {
    return {
      icon: <PriorityHighOutlinedIcon fontSize="small" />,
      bg: alpha("#C62828", 0.10),
      border: "1px solid rgba(198,40,40,0.18)",
      iconColor: "#C62828",
    };
  }

  if (normalized.includes("HIGH PRIORITY")) {
    return {
      icon: <PriorityHighOutlinedIcon fontSize="small" />,
      bg: alpha("#FB8500", 0.12),
      border: "1px solid rgba(251,133,0,0.20)",
      iconColor: "#C96D00",
    };
  }

  return {
    icon: <ConfirmationNumberOutlinedIcon fontSize="small" />,
    bg: alpha("#023047", 0.08),
    border: "1px solid rgba(2,48,71,0.12)",
    iconColor: "#023047",
  };
}

function SummaryMetricCard({
  metric,
  tone = "default",
}: {
  metric: SummaryMetric;
  tone?: "default" | "completed";
}) {
  const visual = metricVisual(metric.label);

  return (
    <Paper
      variant="outlined"
      sx={{
        px: 1,
        py: 0.85,
        borderRadius: 1.75,
        backgroundColor:
          tone === "completed" ? "rgba(248,251,252,0.92)" : "rgba(255,255,255,0.84)",
        border: visual.border,
        minWidth: 0,
        height: "100%",
      }}
    >
      <Stack spacing={0.45}>
        <Stack
          direction="row"
          justifyContent="space-between"
          alignItems="flex-start"
          spacing={0.6}
        >
          <Typography
            variant="caption"
            color="text.secondary"
            sx={{
              fontWeight: 800,
              lineHeight: 1.2,
              minWidth: 0,
              display: "-webkit-box",
              WebkitLineClamp: 2,
              WebkitBoxOrient: "vertical",
              overflow: "hidden",
            }}
          >
            {metric.label}
          </Typography>

          <Box
            sx={{
              width: 24,
              height: 24,
              borderRadius: 1.25,
              bgcolor: visual.bg,
              color: visual.iconColor,
              display: "grid",
              placeItems: "center",
              flexShrink: 0,
            }}
          >
            {visual.icon}
          </Box>
        </Stack>

        <Typography
          variant="h6"
          sx={{
            fontWeight: 900,
            lineHeight: 1.05,
            color: "text.primary",
          }}
        >
          {metric.value}
        </Typography>
      </Stack>
    </Paper>
  );
}

function MetricsGroup({
  title,
  metrics,
  tone = "default",
  columnsDesktop,
}: {
  title: string;
  metrics: SummaryMetric[];
  tone?: "default" | "completed";
  columnsDesktop?: number;
}) {
  return (
    <Stack spacing={0.7}>
      <Typography
        variant="caption"
        sx={{
          fontWeight: 900,
          color: "text.secondary",
          textTransform: "uppercase",
          letterSpacing: "0.04em",
        }}
      >
        {title}
      </Typography>

      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: {
            xs: "repeat(2, minmax(0, 1fr))",
            md: `repeat(${columnsDesktop ?? metrics.length}, minmax(0, 1fr))`,
          },
          gap: 0.85,
        }}
      >
        {metrics.map((metric) => (
          <SummaryMetricCard
            key={`${title}-${metric.label}`}
            metric={metric}
            tone={tone}
          />
        ))}
      </Box>
    </Stack>
  );
}

export default function MetricsSummaryStrip({
  activeTitle = "Active",
  completedTitle = "Completed",
  activeMetrics,
  completedMetrics,
}: {
  activeTitle?: string;
  completedTitle?: string;
  activeMetrics: SummaryMetric[];
  completedMetrics?: SummaryMetric[];
}) {
  const hasCompleted = Boolean(completedMetrics && completedMetrics.length > 0);

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 1.15,
        borderRadius: 2,
        backgroundColor: "transparent",
        backgroundImage: "none",
        boxShadow: "none",
        borderColor: "transparent",
      }}
    >
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: hasCompleted
            ? { xs: "1fr", xl: "1.65fr auto 0.7fr" }
            : { xs: "1fr" },
          gap: 1.1,
          alignItems: "stretch",
        }}
      >
        <MetricsGroup
          title={activeTitle}
          metrics={activeMetrics}
          columnsDesktop={activeMetrics.length}
        />

        {hasCompleted && (
          <>
            <Divider
              orientation="vertical"
              flexItem
              sx={{
                display: { xs: "none", xl: "block" },
                borderColor: "rgba(2,48,71,0.10)",
              }}
            />

            <Divider
              sx={{
                display: { xs: "block", xl: "none" },
                borderColor: "rgba(2,48,71,0.10)",
              }}
            />

            <MetricsGroup
              title={completedTitle}
              metrics={completedMetrics!}
              tone="completed"
              columnsDesktop={completedMetrics!.length}
            />
          </>
        )}
      </Box>
    </Paper>
  );
}