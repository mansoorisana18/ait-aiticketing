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
import ArticleOutlinedIcon from "@mui/icons-material/ArticleOutlined";

import type { TicketResponseBean } from "../../../api/types";
import { formatDateTime } from "../../../utils/dateTime";
import { formatConfidence, formatSimilarity } from "../../../utils/metricsFormat";

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
  onOpenPrimaryLink,
  onOpenConfirmedDuplicates,
  disablePrimaryLinkAction = false,
  disableConfirmedDuplicatesAction = false,
  hideSectionHeader = false,
}: {
  ticket: TicketResponseBean;
  onOpenTextHistory?: () => void;
  hasTextHistory?: boolean;
  onOpenPrimaryLink?: () => void;
  onOpenConfirmedDuplicates?: () => void;
  disablePrimaryLinkAction?: boolean;
  disableConfirmedDuplicatesAction?: boolean;
  hideSectionHeader?: boolean;
}) {
  const duplicateState = (ticket.duplicateState ?? "NONE").toString().toUpperCase();
  const kbSuggestionStatus = (ticket.kbSuggestionStatus ?? "").toString().toUpperCase();
  // const kbSuggestionSource = (ticket.kbSuggestionSource ?? "").toString().toUpperCase();
  const draftKbStatus = (ticket.draftKbStatus ?? "").toString().toUpperCase();

  //Pipeline state calculation.
  const classificationFailed = Boolean(ticket.aiFailed);
  const classificationReady = Boolean(ticket.aiCategory || ticket.aiPriority || ticket.aiTriagedAt);
  const vagueActive =
    ticket.status === "VAGUE" ||
    Boolean(ticket.vagueReason) ||
    Boolean(ticket.clarificationPrompt);

  const duplicatePending =
    ticket.status === "NEW" ||
    ticket.status === "AI_PROCESSING" ||
    ticket.status === "VAGUE";

  const duplicateAwaitingReview =
    ticket.status === "DUPLICATE_REVIEW" || duplicateState === "POTENTIAL";

  const duplicateStatusLabel =
    duplicateState === "CONFIRMED"
      ? "Confirmed duplicate"
      : duplicateAwaitingReview
      ? "Needs review"
      : duplicatePending
      ? "Pending"
      : "No duplicate found";

  const duplicateStatusTone =
    duplicateState === "CONFIRMED"
      ? "info"
      : duplicateAwaitingReview
      ? "warning"
      : duplicatePending
      ? "info"
      : "success";

  const kbSuggestionPending =
    ticket.status === "NEW" ||
    ticket.status === "AI_PROCESSING" ||
    ticket.status === "VAGUE" ||
    ticket.status === "DUPLICATE_REVIEW";

  const kbSuggestionSkippedForDuplicate = duplicateState === "CONFIRMED";

  const kbSuggestionBlockedWaitingForUser =
    ticket.status === "KB_SUGGESTED" || kbSuggestionStatus === "SUGGESTED";

  const kbSuggestionAccepted = kbSuggestionStatus === "ACCEPTED";
  const kbSuggestionRejected = kbSuggestionStatus === "REJECTED";

  const kbSuggestionStatusLabel =
    kbSuggestionAccepted
      ? "Accepted"
      : kbSuggestionRejected
      ? "Rejected"
      : kbSuggestionBlockedWaitingForUser
      ? "Waiting on user"
      : kbSuggestionSkippedForDuplicate
      ? "Skipped"
      : kbSuggestionPending
      ? "Pending"
      : ticket.suggestedKbId
      ? "Suggested"
      : "No suggestion";

  const kbSuggestionStatusTone =
    kbSuggestionAccepted
      ? "success"
      : kbSuggestionRejected
      ? "warning"
      : kbSuggestionBlockedWaitingForUser
      ? "warning"
      : kbSuggestionSkippedForDuplicate
      ? "default"
      : kbSuggestionPending
      ? "info"
      : ticket.suggestedKbId
      ? "info"
      : "success";

  const routingBlockedByKbSuggestion = kbSuggestionBlockedWaitingForUser;

  const routingStatusLabel =
    duplicateState === "CONFIRMED"
      ? "Skipped"
      : routingBlockedByKbSuggestion
      ? "Waiting on user"
      : duplicateState === "POTENTIAL" && ticket.status === "DUPLICATE_REVIEW"
      ? "Waiting on review"
      : ticket.assignedToName || ticket.firstAssignedAt
      ? "Completed"
      : "Pending";

  const routingStatusTone =
    duplicateState === "CONFIRMED"
      ? "default"
      : routingBlockedByKbSuggestion
      ? "warning"
      : duplicateState === "POTENTIAL" && ticket.status === "DUPLICATE_REVIEW"
      ? "warning"
      : ticket.assignedToName || ticket.firstAssignedAt
      ? "success"
      : "info";

  const kbDraftEligible = ticket.status === "RESOLVED" || ticket.status === "CLOSED";
  const kbDraftExists = Boolean(ticket.kbDraftExists || ticket.draftKbId);

  const kbDraftStatusLabel =
    !kbDraftEligible
      ? "Not eligible yet"
      : draftKbStatus === "PUBLISHED"
      ? "Published"
      : draftKbStatus === "IN_REVIEW"
      ? "In review"
      : draftKbStatus === "REJECTED"
      ? "Rejected"
      : draftKbStatus === "DRAFT"
      ? "Draft created"
      : kbDraftExists
      ? "Draft created"
      : "Ready to generate";

  const kbDraftStatusTone =
    !kbDraftEligible
      ? "default"
      : draftKbStatus === "PUBLISHED"
      ? "success"
      : draftKbStatus === "IN_REVIEW"
      ? "info"
      : draftKbStatus === "REJECTED"
      ? "warning"
      : draftKbStatus === "DRAFT"
      ? "info"
      : kbDraftExists
      ? "info"
      : "default";

  return (
    <Stack spacing={0.9}>
      {!hideSectionHeader && (
        <Box>
          <Typography sx={{ fontWeight: 1000, mb: 0.1 }}>AI Automation Journey</Typography>
          <Typography variant="body2" color="text.secondary">
            End-to-end AI-assisted lifecycle across intake, duplicate handling, knowledge support,
            routing, and post-resolution knowledge capture.
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
          <InfoLine label="Model confidence" value={formatConfidence(ticket.aiConfidence ?? null)} />
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
          title="Duplicate Detection"
          icon={<ContentCopyOutlinedIcon fontSize="small" />}
          statusLabel={duplicateStatusLabel}         
          statusTone={duplicateStatusTone}
          action={
            <Stack direction={{ xs: "column", sm: "row" }} spacing={0.8}>
              <Button
                variant="outlined"
                size="small"
                onClick={onOpenPrimaryLink}
                disabled={disablePrimaryLinkAction}
              >
                View primary ticket
              </Button>
              <Button
                variant="outlined"
                size="small"
                onClick={onOpenConfirmedDuplicates}
                disabled={disableConfirmedDuplicatesAction}
              >
                View confirmed duplicates
              </Button>
            </Stack>
          }
        >
          <InfoLine
            label="Duplicate state"
            value={
              duplicatePending
                ? "Pending"
                : duplicateAwaitingReview
                ? "POTENTIAL"
                : duplicateState
            }
          />
          <InfoLine
            label="Reason"
            value={
              duplicatePending
                ? "Duplicate detection has not run yet because the ticket is still in triage or waiting for clarification."
                : duplicateAwaitingReview
                ? ticket.duplicateReason ?? "This ticket has been flagged for duplicate review."
                : ticket.duplicateReason ?? "No duplicate concern detected."
            }
            multiline
          />
          <InfoLine
            label="Confidence"
            value={formatConfidence(ticket.duplicateConfidence ?? null)}
          />
          <InfoLine label="Similarity" value={formatSimilarity(ticket.duplicateSimilarity ?? null)} />
          <InfoLine
            label="Primary ticket"
            value={
              ticket.primaryTicketId
                ? `${ticket.primaryTicketTitle ?? "Ticket"} (Ticket #${ticket.primaryTicketId})`
                : "Not linked"
            }
          />
          <InfoLine label="Link type" value={ticket.duplicateLinkType ?? "—"} />
          <InfoLine label="Link status" value={ticket.duplicateLinkStatus ?? "—"} />
        </StageCard>

        <StageCard
          stepNo={4}
          title="KB Suggestion"
          icon={<AutoAwesomeOutlinedIcon fontSize="small" />}
          statusLabel={kbSuggestionStatusLabel}
          statusTone={kbSuggestionStatusTone}
        >
          <InfoLine
            label="Suggestion outcome"
            value={
              kbSuggestionSkippedForDuplicate
                ? "Skipped because the ticket was confirmed as a duplicate."
                : kbSuggestionPending
                ? "KB suggestion has not run yet because earlier pipeline stages are not complete."
                : kbSuggestionBlockedWaitingForUser
                ? "Waiting for the user to review the suggested article and respond."
                : kbSuggestionAccepted
                ? "The user accepted the suggested article as solving the issue."
                : kbSuggestionRejected
                ? "The user rejected the suggested article and the ticket can continue toward further help."
                : ticket.suggestedKbId
                ? "A KB article was suggested for potential self-resolution."
                : "No KB suggestion was made for this ticket."
            }
            multiline
          />
          <InfoLine
            label="Suggested article"
            value={
              ticket.suggestedKbId
                ? `${ticket.suggestedKbTitle ?? "KB Article"} (KB #${ticket.suggestedKbId})`
                : "No article suggested"
            }
          />
          <InfoLine
            label="Preview"
            value={ticket.suggestedKbPreview ?? "No KB preview available"}
            multiline
          />
          <InfoLine label="Suggestion status" value={ticket.kbSuggestionStatus ?? "—"} />
          <InfoLine label="Suggestion source" value={ticket.kbSuggestionSource ?? "—"} />
          <InfoLine
            label="Similarity"
            value={formatSimilarity(ticket.suggestedKbSimilarity ?? null)}
          />
        </StageCard>

        <StageCard
          stepNo={5}
          title="Routing"
          icon={<RouteOutlinedIcon fontSize="small" />}
          statusLabel={routingStatusLabel}
          statusTone={routingStatusTone}
        >
          <InfoLine label="Assigned to" value={ticket.assignedToName ?? "Unassigned"} />
          <InfoLine label="First assigned on" value={formatDateTime(ticket.firstAssignedAt)} />
          <InfoLine label="Current internal status" value={ticket.status ?? "—"} />
          <InfoLine
            label="Routing outcome"
            value={
              duplicateState === "CONFIRMED"
                ? "Routing not needed because this ticket is linked to a primary ticket."
                : routingBlockedByKbSuggestion
                ? "Routing is waiting for the user's response to the suggested KB article."
                : duplicateState === "POTENTIAL" && ticket.status === "DUPLICATE_REVIEW"
                ? "Routing is waiting for the duplicate review decision."
                : ticket.assignedToName || ticket.firstAssignedAt
                ? "Routing completed successfully."
                : "Routing has not yet been performed."
            }
            multiline
          />
        </StageCard>

        <StageCard
          stepNo={6}
          title="KB Drafting"
          icon={<ArticleOutlinedIcon fontSize="small" />}
          statusLabel={kbDraftStatusLabel}
          statusTone={kbDraftStatusTone}
        >
          <InfoLine
            label="Eligibility"
            value={
              kbDraftEligible
                ? "This ticket is eligible for KB drafting because it has reached a terminal resolution state."
                : "KB drafting becomes available after the assigned agent resolves the ticket."
            }
            multiline
          />
          <InfoLine
            label="Draft article"
            value={
              ticket.draftKbId
                ? `${ticket.draftKbTitle ?? "KB Draft"} (KB #${ticket.draftKbId})`
                : "No draft linked yet"
            }
          />
          <InfoLine label="Draft status" value={ticket.draftKbStatus ?? "—"} />
          <InfoLine
            label="AI generated"
            value={
              ticket.kbDraftAiGenerated == null
                ? "—"
                : ticket.kbDraftAiGenerated
                ? "Yes"
                : "No"
            }
          />
          <InfoLine label="Last draft update" value={formatDateTime(ticket.draftKbUpdatedAt)} />
        </StageCard>
      </Box>
    </Stack>
  );
}