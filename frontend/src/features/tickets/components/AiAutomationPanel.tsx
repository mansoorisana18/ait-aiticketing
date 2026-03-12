import React from "react";
import {
  Box,
  Button,
  Chip,
  Divider,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { alpha } from "@mui/material/styles";
import ManageSearchOutlinedIcon from "@mui/icons-material/ManageSearchOutlined";
import HelpOutlineOutlinedIcon from "@mui/icons-material/HelpOutlineOutlined";
import RouteOutlinedIcon from "@mui/icons-material/RouteOutlined";
import ContentCopyOutlinedIcon from "@mui/icons-material/ContentCopyOutlined";
import AutoAwesomeOutlinedIcon from "@mui/icons-material/AutoAwesomeOutlined";

import type { TicketResponseBean } from "../../../api/types";
import { formatDateTime } from "../../../utils/dateTime";

function parseConfidence(aiConfidence: TicketResponseBean["aiConfidence"]): string {
  if (aiConfidence == null) return "—";
  const n = typeof aiConfidence === "string" ? Number(aiConfidence) : aiConfidence;
  if (Number.isNaN(n)) return "—";
  if (n <= 1) return `${Math.round(n * 100)}%`;
  if (n <= 100) return `${Math.round(n)}%`;
  return "—";
}

/** Reusable value line inside each AI pipeline stage card. */
function InfoLine({
  label,
  value,
  multiline = false,
}: {
  label: string;
  value: React.ReactNode;
  multiline?: boolean;
}) {
  return (
    <Box>
      <Typography variant="caption" sx={{ color: "text.secondary", fontWeight: 700, display: "block", mb: 0.1 }}>
        {label}
      </Typography>
      <Typography
        variant="body2"
        sx={{
          fontWeight: 800,
          color: "text.primary",
          whiteSpace: multiline ? "pre-wrap" : "normal",
          wordBreak: "break-word",
          lineHeight: 1.4,
        }}
      >
        {value}
      </Typography>
    </Box>
  );
}

/**
 * Pipeline stage chip logic:
 * success => stage complete
 * warning => attention / clarification needed
 * error   => failure
 * info    => partial / pending operational state
 * default => future stages
 */
function StageChip({
  label,
  tone,
}: {
  label: string;
  tone: "success" | "warning" | "error" | "info" | "default";
}) {
  const sxMap = {
    success: {
      bgcolor: alpha("#15803D", 0.10),
      color: "#15803D",
      border: "1px solid rgba(21,128,61,0.18)",
    },
    warning: {
      bgcolor: alpha("#FFB703", 0.20),
      color: "#875F00",
      border: "1px solid rgba(255,183,3,0.28)",
    },
    error: {
      bgcolor: alpha("#C62828", 0.10),
      color: "#C62828",
      border: "1px solid rgba(198,40,40,0.18)",
    },
    info: {
      bgcolor: alpha("#219EBC", 0.12),
      color: "#0E6B84",
      border: "1px solid rgba(33,158,188,0.18)",
    },
    default: {
      bgcolor: alpha("#023047", 0.06),
      color: "#4A6070",
      border: "1px solid rgba(2,48,71,0.10)",
    },
  };

  return <Chip size="small" label={label} sx={{ fontWeight: 800, ...sxMap[tone] }} />;
}

/** Card base for a stage of the AI pipeline. */
function StageCard({
  stepNo,
  title,
  icon,
  statusLabel,
  statusTone,
  children,
  muted = false,
  action,
}: {
  stepNo: number;
  title: string;
  icon: React.ReactNode;
  statusLabel: string;
  statusTone: "success" | "warning" | "error" | "info" | "default";
  children: React.ReactNode;
  muted?: boolean;
  action?: React.ReactNode;
}) {
  return (
    <Paper
      variant="outlined"
      sx={{
        p: 1,
        borderRadius: 1.75,
        border: muted
          ? "1px dashed rgba(2,48,71,0.16)"
          : "1px solid rgba(2,48,71,0.10)",
        background: muted
          ? "linear-gradient(180deg, rgba(255,255,255,0.96), rgba(245,249,251,1))"
          : "linear-gradient(180deg, rgba(255,255,255,1), rgba(248,252,254,1))",
        opacity: muted ? 0.82 : 1,
        height: "100%",
        boxShadow: muted ? "none" : "0 2px 8px rgba(2,48,71,0.05)",
        overflow: "hidden",
        position: "relative",
        zIndex: 1,
      }}
    >
      <Stack spacing={0.8} sx={{ height: "100%" }}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start" spacing={0.7}>
          <Stack direction="row" spacing={0.7} alignItems="center" sx={{ minWidth: 0 }}>
            <Box
              sx={{
                width: 20,
                height: 20,
                borderRadius: "50%",
                bgcolor: muted ? alpha("#023047", 0.06) : "primary.main",
                color: muted ? "text.secondary" : "primary.contrastText",
                display: "grid",
                placeItems: "center",
                fontWeight: 900,
                flexShrink: 0,
              }}
            >
              {stepNo}
            </Box>

            <Box
              sx={{
                width: 26,
                height: 26,
                borderRadius: 1.25,
                bgcolor: muted ? alpha("#023047", 0.05) : alpha("#219EBC", 0.10),
                color: muted ? "text.secondary" : "primary.main",
                display: "grid",
                placeItems: "center",
                flexShrink: 0,
              }}
            >
              {icon}
            </Box>

            <Typography sx={{ fontWeight: 900, lineHeight: 1.15, minWidth: 0 }}>
              {title}
            </Typography>
          </Stack>

          <StageChip label={statusLabel} tone={statusTone} />
        </Stack>

        <Divider />

        <Stack spacing={0.7} sx={{ flexGrow: 1, minWidth: 0 }}>
          {children}
        </Stack>

        {action && <Box sx={{ pt: 0.1 }}>{action}</Box>}
      </Stack>
    </Paper>
  );
}

export default function AiAutomationPanel({
  ticket,
  onOpenTextHistory,
  hasTextHistory,
  hideSectionHeader = false,
}: {
  ticket: TicketResponseBean;
  onOpenTextHistory?: () => void;
  hasTextHistory?: boolean;
  hideSectionHeader?: boolean;
}) {
  const confidence = parseConfidence(ticket.aiConfidence);
  const duplicateState = (ticket.duplicateState ?? "NONE").toString().toUpperCase();

  // Pipeline state calculation.
  const classificationFailed = Boolean(ticket.aiFailed);
  const classificationReady = Boolean(ticket.aiCategory || ticket.aiPriority || ticket.aiTriagedAt);
  const vagueActive =
    ticket.status === "VAGUE" ||
    Boolean(ticket.vagueReason) ||
    Boolean(ticket.clarificationPrompt);
  const routingReady = Boolean(ticket.assignedToName || ticket.firstAssignedAt);
  const duplicateSignalPresent = duplicateState !== "NONE";

  return (
    <Stack spacing={0.9}>
      {!hideSectionHeader && (
        <Box>
          <Typography sx={{ fontWeight: 1000, mb: 0.1 }}>AI Automation Journey</Typography>
          <Typography variant="body2" color="text.secondary">
            Core AI triage stages for classification, vague detection, routing, and future automation.
          </Typography>
        </Box>
      )}

      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: {
            xs: "1fr",
            md: "repeat(2, minmax(0, 1fr))",
            xl: "repeat(3, minmax(0, 1fr))",
          },
          gap: 0.9,
        }}
      >
        <StageCard
          stepNo={1}
          title="Classification"
          icon={<ManageSearchOutlinedIcon fontSize="small" />}
          statusLabel={
            classificationFailed ? "Failed" : classificationReady ? "Completed" : "Pending"
          }
          statusTone={classificationFailed ? "error" : classificationReady ? "success" : "info"}
        >
          <InfoLine label="Detected category" value={ticket.aiCategory ?? "Not available"} />
          <InfoLine label="Assigned priority" value={ticket.aiPriority ?? "Not available"} />
          <InfoLine label="Model confidence" value={confidence} />
          <InfoLine label="AI analyzed on" value={formatDateTime(ticket.aiTriagedAt)} />
          {ticket.aiLastError && (
            <InfoLine label="Latest AI issue" value={ticket.aiLastError} multiline />
          )}
        </StageCard>

        <StageCard
          stepNo={2}
          title="Vague Detection"
          icon={<HelpOutlineOutlinedIcon fontSize="small" />}
          statusLabel={
            vagueActive
              ? "Needs clarification"
              : ticket.vagueCount && ticket.vagueCount > 0
              ? "Handled"
              : "Not needed"
          }
          statusTone={
            vagueActive
              ? "warning"
              : ticket.vagueCount && ticket.vagueCount > 0
              ? "info"
              : "success"
          }
          action={
            hasTextHistory && onOpenTextHistory ? (
              <Button variant="outlined" size="small" onClick={onOpenTextHistory}>
                View ticket text history
              </Button>
            ) : undefined
          }
        >
          <InfoLine label="Clarification requests sent" value={ticket.vagueCount ?? 0} />
          <InfoLine label="Last clarification request" value={formatDateTime(ticket.lastVagueAt)} />
          <InfoLine
            label="Why clarification was needed"
            value={ticket.vagueReason ?? "No missing information detected"}
            multiline
          />
          <InfoLine
            label="Required from user"
            value={ticket.clarificationPrompt ?? "No clarification requested"}
            multiline
          />
          <InfoLine label="Current ticket version" value={ticket.currentTextVersion ?? "—"} />
        </StageCard>

        <StageCard
          stepNo={3}
          title="Routing"
          icon={<RouteOutlinedIcon fontSize="small" />}
          statusLabel={routingReady ? "Completed" : "Pending"}
          statusTone={routingReady ? "success" : "info"}
        >
          <InfoLine label="Assigned to" value={ticket.assignedToName ?? "Unassigned"} />
          <InfoLine label="First assigned on" value={formatDateTime(ticket.firstAssignedAt)} />
          <InfoLine label="Current internal status" value={ticket.status ?? "—"} />
        </StageCard>

        <StageCard
          stepNo={4}
          title="Duplicate Detection"
          icon={<ContentCopyOutlinedIcon fontSize="small" />}
          statusLabel="TO DO"
          // statusLabel={duplicateSignalPresent ? "Flag available" : "In progress"}
          statusTone={duplicateSignalPresent ? "info" : "default"}
          muted
        >
          <InfoLine label="Duplicate review result" value={duplicateState} />
          <InfoLine
            label="Workflow note"
            value="Duplicate detection possible values NONE, POTENTIAL, CONFIRMED"
            multiline
          />
        </StageCard>

        <StageCard
          stepNo={5}
          title="Knowledge Assist"
          icon={<AutoAwesomeOutlinedIcon fontSize="small" />}
          statusLabel="TO DO"
          statusTone="default"
          muted
        >
          <InfoLine
            label="Planned capabilities"
            value="KB Suggestion and KB draft generation features"
            multiline
          />
        </StageCard>
      </Box>
    </Stack>
  );
}