import React from "react";
import { useParams } from "react-router-dom";
import { Accordion, AccordionDetails, AccordionSummary, Alert, Box, Button, Chip, Divider, MenuItem, Paper, Stack, TextField, Typography, } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { statusChipSx } from "../components/statusColors";
import AiAutomationPanel from "../components/AiAutomationPanel";
import { formatDateTime } from "../../../utils/dateTime";

import type {
  AdminOverrideRequestBean,
  AdminOverrideType,
  TicketResponseBean,
  TicketStatus,
  UpdateTicketStatusRequestBean,
  UserTicketResponseBean,
} from "../../../api/types";

import {
  useAdminOverride,
  useAgentUpdateStatus,
  useTicketDetailsInternal,
  useTicketDetailsUser,
} from "../hooks";

import TicketDetailsComments from "../components/TicketDetailsComments";

// ---- Dropdown options ----
const INTERNAL_STATUS_OPTIONS: TicketStatus[] = [
  "NEW",
  "AI_PROCESSING",
  "VAGUE",
  "READY",
  "IN_PROGRESS",
  "DUPLICATE",
  "RESOLVED",
  "CLOSED",
];

const AGENT_STATUS_OPTIONS: UpdateTicketStatusRequestBean["status"][] = ["IN_PROGRESS", "RESOLVED", "CLOSED"];

const OVERRIDE_TYPES: AdminOverrideType[] = ["STATUS", "CATEGORY", "PRIORITY", "DUPLICATE_LINK", "KB_DRAFT", "ASSIGNMENT"];

const CATEGORY_OPTIONS = [
  "TECHNICAL SUPPORT",
  "BILLING AND PAYMENTS",
  "ORDERS AND RETURNS",
  "SALES AND PRESALES",
  "ACCOUNT AND ACCESS",
  "GENERAL INQUIRY",
];

const PRIORITY_OPTIONS = ["LOW", "MEDIUM", "HIGH", "URGENT"];
const DUPLICATE_STATE_OPTIONS = ["NONE", "POTENTIAL", "CONFIRMED"];

const CARD_SX = {
  p: 2,
  borderRadius: 2,
  border: "1px solid rgba(138,86,172,0.12)",
} as const;

export default function TicketDetailsPage() {
  const { auth } = useAuth();
  const { ticketId } = useParams();
  const idNum = ticketId ? Number(ticketId) : null;

  const isUser = auth.role === "USER";
  const isAgent = auth.role === "AGENT";
  const isAdmin = auth.role === "ADMIN";

  const userQuery = useTicketDetailsUser(idNum, isUser);
  const internalQuery = useTicketDetailsInternal(idNum, !isUser);

  const isLoading = isUser ? userQuery.isLoading : internalQuery.isLoading;

  const userTicket = userQuery.data as UserTicketResponseBean | undefined;
  const internalTicket = internalQuery.data as TicketResponseBean | undefined;

  // ---- Agent status update form ----
  const agentUpdate = useAgentUpdateStatus(idNum ?? -1);
  const [agentStatus, setAgentStatus] = React.useState<UpdateTicketStatusRequestBean["status"]>("IN_PROGRESS");


  // ---- Admin override form ----
  const adminOverride = useAdminOverride(idNum ?? -1);
  const [overrideType, setOverrideType] = React.useState<AdminOverrideType>("STATUS");
  const [newValue, setNewValue] = React.useState<string>("");
  const [newAssignedToUserId, setNewAssignedToUserId] = React.useState<number | null>(null);
  const [reason, setReason] = React.useState<string>("");

  // Initialize sensible defaults from ticket when it loads
  React.useEffect(() => {
    if (!internalTicket) return;

    // agent default status = current status if allowed; else IN_PROGRESS
    if (isAgent) {
      const current = internalTicket.status;
      if (current === "IN_PROGRESS" || current === "RESOLVED" || current === "CLOSED") {
        setAgentStatus(current);
      } else {
        setAgentStatus("IN_PROGRESS");
      }
    }

    // admin override default newValue based on type
    if (isAdmin) {
      if (overrideType === "STATUS") setNewValue(internalTicket.status);
      if (overrideType === "CATEGORY") setNewValue(internalTicket.aiCategory ?? "");
      if (overrideType === "PRIORITY") setNewValue(internalTicket.aiPriority ?? "");
      if (overrideType === "DUPLICATE_LINK") setNewValue((internalTicket.duplicateState ?? "").toString());
      if (overrideType === "ASSIGNMENT") setNewAssignedToUserId(internalTicket.assignedToUserId ?? null);
    }
  }, [internalTicket, isAgent, isAdmin, overrideType]);

  if (isLoading) return <LoadingSkeleton variant="detail" />;
  if (!idNum) return <Typography>Invalid ticket id.</Typography>;

  if (isUser && !userTicket) return <Typography>Ticket not found.</Typography>;
  if (!isUser && !internalTicket) return <Typography>Ticket not found.</Typography>;

  const headerTitle = isUser ? userTicket!.title : internalTicket!.title;
  const headerId = isUser ? userTicket!.ticketId : internalTicket!.ticketId;

  const userStatus = isUser ? userTicket!.userTicketStatus : internalTicket!.userTicketStatus;
  const internalStatus = !isUser ? internalTicket!.status : undefined;

  const canAgentUpdate =
    isAgent &&
    internalTicket!.assignedToUserId != null &&
    internalTicket!.assignedToUserId === auth.userId;

  const submitAgentStatus = async () => {
    if (!canAgentUpdate || !idNum) return;
    await agentUpdate.mutateAsync({ status: agentStatus });
  };

  const submitAdminOverride = async () => {
    if (!isAdmin || !idNum) return;

    const payload: AdminOverrideRequestBean = {
      overrideType,
      reason: reason.trim() || null,
    };

    if (overrideType === "ASSIGNMENT") {
      payload.newAssignedToUserId = newAssignedToUserId ?? null; // null = unassign
    } else {
      payload.newValue = newValue.trim() || "";
    }

    await adminOverride.mutateAsync(payload);
    setReason("");
  };

  const renderOverrideNewValueField = () => {
    if (overrideType === "ASSIGNMENT") {
      return (
        <TextField
          label="Assign to Agent User ID (blank = unassign)"
          type="number"
          value={newAssignedToUserId ?? ""}
          onChange={(e) => setNewAssignedToUserId(e.target.value === "" ? null : Number(e.target.value))}
          helperText="Later we can replace this with an Agents dropdown."
        />
      );
    }

    if (overrideType === "STATUS") {
      return (
        <TextField select label="New Internal Status" value={newValue} onChange={(e) => setNewValue(e.target.value)}>
          {INTERNAL_STATUS_OPTIONS.map((s) => (
            <MenuItem key={s} value={s}>
              {s}
            </MenuItem>
          ))}
        </TextField>
      );
    }

    if (overrideType === "CATEGORY") {
      return (
        <TextField select label="New Category" value={newValue} onChange={(e) => setNewValue(e.target.value)}>
          <MenuItem value="">—</MenuItem>
          {CATEGORY_OPTIONS.map((c) => (
            <MenuItem key={c} value={c}>
              {c}
            </MenuItem>
          ))}
        </TextField>
      );
    }

    if (overrideType === "PRIORITY") {
      return (
        <TextField select label="New Priority" value={newValue} onChange={(e) => setNewValue(e.target.value)}>
          <MenuItem value="">—</MenuItem>
          {PRIORITY_OPTIONS.map((p) => (
            <MenuItem key={p} value={p}>
              {p}
            </MenuItem>
          ))}
        </TextField>
      );
    }

    if (overrideType === "DUPLICATE_LINK") {
      return (
        <TextField
          select
          label="New Duplicate State"
          value={newValue}
          onChange={(e) => setNewValue(e.target.value)}
        >
          <MenuItem value="">—</MenuItem>
          {DUPLICATE_STATE_OPTIONS.map((d) => (
            <MenuItem key={d} value={d}>
              {d}
            </MenuItem>
          ))}
        </TextField>
      );
    }

    // KB_DRAFT: free text
    return (
      <TextField
        label="New Value"
        value={newValue}
        onChange={(e) => setNewValue(e.target.value)}
        multiline
        minRows={3}
      />
    );
  };

  return (
    <Stack spacing={2} sx={{ width: "100%" }}>
      {/* -------------------- Band 1: Overview -------------------- */}
      <Paper variant="outlined" sx={{ ...CARD_SX, p: 2.5 }}>
        <Stack spacing={2}>
          {/* Title row */}
          <Stack
            direction={{ xs: "column", md: "row" }}
            justifyContent="space-between"
            alignItems={{ xs: "flex-start", md: "center" }}
            spacing={1.25}
          >
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 1000 }}>
              {headerTitle}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Ticket #{headerId}
            </Typography>
          </Box>

          <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
            <Chip label={`User: ${userStatus}`} size="small" sx={{ ...statusChipSx(userStatus), fontWeight: 700 }} />
            {!isUser && <Chip label={`Internal: ${internalStatus}`} size="small" sx={{ ...statusChipSx(internalStatus), fontWeight: 700 }} />}
          </Stack>
        </Stack>

        <Divider />

        {/* Content row: Description (left) + Details (right) */}
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", md: "1.6fr 1fr" },
            gap: 2,
            alignItems: "stretch",
          }}
        >
          {/* LEFT: Description */}
          <Paper variant="outlined" sx={CARD_SX}>
            <Typography sx={{ fontWeight: 1000, mb: 0.75 }}>Description</Typography>

            <Typography sx={{ whiteSpace: "pre-wrap" }}>
              {(isUser ? userTicket!.description : internalTicket!.description) ?? ""}
            </Typography>
          </Paper>

          {/* RIGHT: Details (Created/Updated + CreatedBy + Assigned) */}
          <Paper variant="outlined" sx={CARD_SX}>
            <Typography sx={{ fontWeight: 1000, mb: 0.75 }}>Details</Typography>

            <Stack spacing={1}>
              <Typography variant="body2" color="text.secondary">
                Created: {formatDateTime(isUser ? userTicket!.createdAt : internalTicket!.createdAt)}
              </Typography>

              <Typography variant="body2" color="text.secondary">
                Updated: {formatDateTime(isUser ? userTicket!.updatedAt : internalTicket!.updatedAt)}
              </Typography>

              <Divider sx={{ my: 0.5 }} />

              {/* Created by (shown to USER too) */}
              <Typography variant="body2" sx={{ fontWeight: 900 }}>
                Created by: {isUser ? userTicket!.createdByName : internalTicket!.createdByName}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {isUser ? userTicket!.createdByEmail : internalTicket!.createdByEmail}
              </Typography>

              <Divider sx={{ my: 0.5 }} />

              {/* Assigned: on the same line */}
              <Stack direction="row" spacing={1} alignItems="baseline" flexWrap="wrap">
                <Typography variant="body2" sx={{ fontWeight: 1000 }}>
                  Assigned:
                </Typography>

                <Typography variant="body2" sx={{ fontWeight: 900 }}>
                  {isUser ? userTicket!.assignedToName ?? "Unassigned" : internalTicket!.assignedToName ?? "Unassigned"}
                </Typography>
              </Stack>

              {/* Assigned email visible to USER too */}
              {!isUser ? (
                <Typography variant="body2" color="text.secondary">
                  {internalTicket!.assignedToEmail ?? ""}
                </Typography>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    {/* If the UserTicketResponseBean does not include assignedToEmail, this will be blank. */}
                    {(userTicket as any).assignedToEmail ?? ""}
                  </Typography>
                )}
              </Stack>
            </Paper>
          </Box>
        </Stack>
      </Paper>

      {/* -------------------- Band 2: AI + Role actions -------------------- */}
      {/* Show Band 2 ONLY for internal roles */}
      {!isUser && internalTicket && (
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
            gap: 2,
            alignItems: "stretch",
          }}
        >
          {/* Left: AI Automation */}
          <Box sx={{ minWidth: 0 }}>
            <AiAutomationPanel ticket={internalTicket} />
          </Box>

          {/* Right: ADMIN override OR AGENT status update */}
          <Box sx={{ minWidth: 0 }}>
            {isAdmin ? (
              <Paper variant="outlined" sx={{ ...CARD_SX, p: 2.5 }}>
                <Typography sx={{ fontWeight: 1000, mb: 1 }}>Admin Override</Typography>

                <Stack spacing={2}>
                  <TextField
                    select
                    label="Override Type"
                    value={overrideType}
                    onChange={(e) => {
                      const t = e.target.value as AdminOverrideType;
                      setOverrideType(t);
                      setNewValue("");
                      setNewAssignedToUserId(null);
                      setReason("");
                    }}
                  >
                    {OVERRIDE_TYPES.map((t) => (
                      <MenuItem key={t} value={t}>
                        {t}
                      </MenuItem>
                    ))}
                  </TextField>

                  {renderOverrideNewValueField()}

                  <TextField
                    label="Reason (optional)"
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    multiline
                    minRows={2}
                  />

                  <Button variant="contained" onClick={submitAdminOverride} disabled={adminOverride.isPending}>
                    {adminOverride.isPending ? "Applying…" : "Apply Override"}
                  </Button>

                  {adminOverride.isError && <Alert severity="error">Failed to apply override.</Alert>}
                  {adminOverride.isSuccess && <Alert severity="success">Override applied.</Alert>}
                </Stack>
              </Paper>
            ) : (
              // AGENT ONLY (no admin override text shown to user/admin? -> only agent panel here)
              <Paper variant="outlined" sx={{ ...CARD_SX, p: 2.5 }}>
                <Typography sx={{ fontWeight: 1000, mb: 1 }}>Update Status</Typography>

                {!canAgentUpdate ? (
                  <Alert severity="info">You can update status only for tickets assigned to you.</Alert>
                ) : (
                  <Stack spacing={2}>
                    <TextField
                      select
                      label="New Status"
                      value={agentStatus}
                      onChange={(e) => setAgentStatus(e.target.value as any)}
                    >
                      {AGENT_STATUS_OPTIONS.map((s) => (
                        <MenuItem key={s} value={s}>
                          {s}
                        </MenuItem>
                      ))}
                    </TextField>

                    <Button variant="contained" onClick={submitAgentStatus} disabled={agentUpdate.isPending}>
                      {agentUpdate.isPending ? "Updating…" : "Update Status"}
                    </Button>

                    {agentUpdate.isError && <Alert severity="error">Failed to update status.</Alert>}
                    {agentUpdate.isSuccess && <Alert severity="success">Status updated.</Alert>}
                  </Stack>
                )}
              </Paper>
            )}
          </Box>
        </Box>
      )}

      {/* -------------------- Band 3: Comments -------------------- */}
      <Box sx={{ width: "100%" }}>
        <TicketDetailsComments ticketId={idNum} role={auth.role ?? "USER"} />
      </Box>
    </Stack>
  );
}