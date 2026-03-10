import React from "react";
import { Divider, Paper, Stack, Typography } from "@mui/material";
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

function Row({
  label,
  value,
  accent,
  multiline = false,
}: {
  label: string;
  value: React.ReactNode;
  accent?: boolean;
  multiline?: boolean;
}) {
  return (
    <Stack direction="row" spacing={2} sx={{ py: 0.75, alignItems: multiline ? "flex-start" : "center" }}>
      <Typography
        variant="body2"
        sx={{ width: 140, flexShrink: 0, fontWeight: 900, color: "text.secondary" }}
      >
        {label}
      </Typography>
      <Typography
        variant="body2"
        sx={{
          fontWeight: 900,
          color: accent ? "primary.main" : "text.primary",
          overflow: multiline ? "visible" : "hidden",
          textOverflow: multiline ? "clip" : "ellipsis",
          whiteSpace: multiline ? "pre-wrap" : "nowrap",
          wordBreak: multiline ? "break-word" : "normal",
        }}
        title={typeof value === "string" && !multiline ? value : undefined}
      >
        {value}
      </Typography>
    </Stack>
  );
}

export default function AiAutomationPanel({ ticket }: { ticket: TicketResponseBean }) {
  const conf = parseConfidence(ticket.aiConfidence);
  const dup = (ticket.duplicateState ?? "NONE").toString().toUpperCase();
  const dupAccent = dup === "CONFIRMED" || dup === "POTENTIAL";

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 2,
        borderRadius: 2,
        border: "1px solid rgba(138,86,172,0.12)",
        height: "100%",
      }}
    >
      <Stack spacing={1}>
        <Typography sx={{ fontWeight: 1000 }}>AI Automation</Typography>
        <Divider />

        {/* Core triage */}
        <Row label="Category" value={ticket.aiCategory ?? "—"} accent />
        <Divider />
        <Row label="Priority" value={ticket.aiPriority ?? "—"} accent />
        <Divider />
        <Row label="Confidence" value={conf} />
        <Divider />
        <Row label="Triaged At" value={formatDateTime(ticket.aiTriagedAt)} />
        <Divider />
        <Row label="AI Failed" value={ticket.aiFailed == null ? "—" : ticket.aiFailed ? "Yes" : "No"} />
        <Divider />
        <Row label="Last Error" value={ticket.aiLastError ?? "—"} multiline />

        <Divider />

        {/* Vague handling */}
        <Row label="Vague Count" value={ticket.vagueCount ?? "—"} />
        <Divider />
        <Row label="Last Vague At" value={formatDateTime(ticket.lastVagueAt)} />
        <Divider />
        <Row label="Vague Reason" value={ticket.vagueReason ?? "—"} multiline />
        <Divider />
        <Row label="Clarification" value={ticket.clarificationPrompt ?? "—"} multiline />

        <Divider />

        {/* Routing / assignment */}
        <Row label="Duplicate" value={dup} accent={dupAccent} />
        <Divider />
        <Row label="Assigned To" value={ticket.assignedToName ?? "Unassigned"} />
        <Divider />
        <Row label="First Assigned" value={formatDateTime(ticket.firstAssignedAt)} />
        <Divider />
        <Row label="Text Version" value={ticket.currentTextVersion ?? "—"} />
      </Stack>
    </Paper>
  );
}