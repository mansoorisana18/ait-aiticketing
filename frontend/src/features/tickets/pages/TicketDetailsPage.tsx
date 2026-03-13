import React from "react";
import { useParams } from "react-router-dom";
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogContent,
  DialogTitle,
  IconButton,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import HelpOutlineIcon from "@mui/icons-material/HelpOutline";
import AutorenewIcon from "@mui/icons-material/Autorenew";
import ContentCopyOutlinedIcon from "@mui/icons-material/ContentCopyOutlined";
import { alpha } from "@mui/material/styles";

import { useAuth } from "../../../state/AuthContext";
import LoadingSkeleton from "../../../components/LoadingSkeleton";
import { statusChipSx } from "../components/statusColors";
import AiAutomationPanel from "../components/AiAutomationPanel";
import TicketDetailsComments from "../components/TicketDetailsComments";
import TicketTextHistoryPanel from "../components/TicketTextHistoryPanel";
import VagueClarificationPanel from "../components/VagueClarificationPanel";
import { formatDateTime } from "../../../utils/dateTime";

import type {
  AdminOverrideRequestBean,
  AdminOverrideType,
  Department,
  TicketResponseBean,
  TicketStatus,
  UpdateTicketStatusRequestBean,
  UserTicketResponseBean,
} from "../../../api/types";

import {
  useAdminOverride,
  useAgentUpdateStatus,
  useClarifyVagueTicket,
  useTicketDetailsInternal,
  useTicketDetailsUser,
  useTicketTextVersionHistory,
} from "../hooks";

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

const AGENT_STATUS_OPTIONS: UpdateTicketStatusRequestBean["status"][] = [
  "IN_PROGRESS",
  "RESOLVED",
  "CLOSED",
];

const OVERRIDE_TYPES: AdminOverrideType[] = [
  "STATUS",
  "CATEGORY",
  "PRIORITY",
  "DUPLICATE_LINK",
  "KB_DRAFT",
  "ASSIGNMENT",
];

const CATEGORY_OPTIONS: Department[] = [
  "TECHNICAL SUPPORT",
  "BILLING AND PAYMENTS",
  "ORDERS AND RETURNS",
  "SALES AND PRESALES",
  "ACCOUNT AND ACCESS",
  "GENERAL INQUIRY",
];

const PRIORITY_OPTIONS = ["LOW", "MEDIUM", "HIGH", "URGENT"];
const DUPLICATE_STATE_OPTIONS = ["NONE", "POTENTIAL", "CONFIRMED"];

/** Shared band/card styling */
const CARD_SX = {
  p: 1.35,
  borderRadius: 2,
  border: "1px solid rgba(2,48,71,0.10)",
  boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
} as const;

/** App bar offset used when smooth-scrolling to Band 3 of AI Automation. */
const APPBAR_SCROLL_OFFSET = 88;

/** Header common ticket metadata pills */
function MetaPill({
  label,
  value,
}: {
  label: string;
  value: React.ReactNode;
}) {
  return (
    <Box
      sx={{
        px: 0.95,
        py: 0.65,
        borderRadius: 1.5,
        border: "1px solid rgba(2,48,71,0.08)",
        backgroundColor: "rgba(255,255,255,0.72)",
        minWidth: 0,
      }}
    >
      <Typography
        variant="caption"
        sx={{ display: "block", color: "text.secondary", fontWeight: 700, mb: 0.1 }}
      >
        {label}
      </Typography>
      <Typography
        variant="body2"
        sx={{
          fontWeight: 800,
          color: "text.primary",
          overflow: "hidden",
          textOverflow: "ellipsis",
          whiteSpace: "nowrap",
        }}
        title={typeof value === "string" ? value : undefined}
      >
        {value}
      </Typography>
    </Box>
  );
}

/** Compact key-value row for current state card. */
function CompactInfoRow({
  label,
  value,
}: {
  label: string;
  value: React.ReactNode;
}) {
  return (
    <Stack direction="row" justifyContent="space-between" spacing={1.1}>
      <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 700 }}>
        {label}
      </Typography>
      <Typography
        variant="body2"
        sx={{ fontWeight: 800, textAlign: "right", wordBreak: "break-word" }}
      >
        {value}
      </Typography>
    </Stack>
  );
}

/** Small summary chip used in Band 2 AI summary strip */
function SummaryChip({
  label,
  tone,
  icon,
}: {
  label: string;
  tone: "success" | "warning" | "info" | "default" | "error";
  icon?: React.ReactNode;
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
    info: {
      bgcolor: alpha("#219EBC", 0.12),
      color: "#0E6B84",
      border: "1px solid rgba(33,158,188,0.18)",
    },
    error: {
      bgcolor: alpha("#C62828", 0.10),
      color: "#C62828",
      border: "1px solid rgba(198,40,40,0.18)",
    },
    default: {
      bgcolor: alpha("#023047", 0.06),
      color: "#4A6070",
      border: "1px solid rgba(2,48,71,0.10)",
    },
  };

  return (
    <Chip
      size="small"
      icon={icon as any}
      label={label}
      sx={{ fontWeight: 800, ...sxMap[tone] }}
    />
  );
}

export default function TicketDetailsPage() {
  const { auth } = useAuth();
  const { ticketId } = useParams();
  const idNum = ticketId ? Number(ticketId) : null;

  const isUser = auth.role === "USER";
  const isAgent = auth.role === "AGENT";
  const isAdmin = auth.role === "ADMIN";

  const userQuery = useTicketDetailsUser(idNum, Boolean(isUser && auth.token));
  const internalQuery = useTicketDetailsInternal(idNum, Boolean(!isUser && auth.token));

  const isLoading = isUser ? userQuery.isLoading : internalQuery.isLoading;

  const userTicket = userQuery.data as UserTicketResponseBean | undefined;
  const internalTicket = internalQuery.data as TicketResponseBean | undefined;

  const clarifyMutation = useClarifyVagueTicket(idNum ?? -1);

  const agentUpdate = useAgentUpdateStatus(idNum ?? -1);
  const [agentStatus, setAgentStatus] =
    React.useState<UpdateTicketStatusRequestBean["status"]>("IN_PROGRESS");

  const adminOverride = useAdminOverride(idNum ?? -1);
  const [overrideType, setOverrideType] = React.useState<AdminOverrideType>("STATUS");
  const [newValue, setNewValue] = React.useState<string>("");
  const [newAssignedToUserId, setNewAssignedToUserId] = React.useState<number | null>(null);
  const [reason, setReason] = React.useState<string>("");

  /** Band 3 AI journey accordion state to be expanded by default */
  const [aiExpanded, setAiExpanded] = React.useState(true);

  /** Dialog for text history which will be opened only from Vague Detection stage action. */
  const [historyOpen, setHistoryOpen] = React.useState(false);

  /** Ref used so the Band 2 "View full journey" button expands and scrolls to Band 3. */
  const aiJourneyRef = React.useRef<HTMLDivElement | null>(null);

  const textHistoryQuery = useTicketTextVersionHistory(
    idNum,
    Boolean(idNum && auth.token && (isAdmin || isAgent))
  );

  React.useEffect(() => {
    if (!internalTicket) return;

    if (isAgent) {
      const current = internalTicket.status;
      if (current === "IN_PROGRESS" || current === "RESOLVED" || current === "CLOSED") {
        setAgentStatus(current);
      } else {
        setAgentStatus("IN_PROGRESS");
      }
    }

    if (isAdmin) {
      if (overrideType === "STATUS") setNewValue(internalTicket.status);
      if (overrideType === "CATEGORY") setNewValue(internalTicket.aiCategory ?? "");
      if (overrideType === "PRIORITY") setNewValue(internalTicket.aiPriority ?? "");
      if (overrideType === "DUPLICATE_LINK") {
        setNewValue((internalTicket.duplicateState ?? "").toString());
      }
      if (overrideType === "ASSIGNMENT") {
        setNewAssignedToUserId(internalTicket.assignedToUserId ?? null);
      }
    }
  }, [internalTicket, isAgent, isAdmin, overrideType]);

  if (isLoading) return <LoadingSkeleton variant="detail" />;
  if (!idNum) return <Typography>Invalid ticket id.</Typography>;

  if (isUser && !userTicket) return <Typography>Ticket not found.</Typography>;
  if (!isUser && !internalTicket) return <Typography>Ticket not found.</Typography>;

  const internalTicketData = !isUser ? internalTicket! : undefined;
  const currentTicket = isUser ? userTicket! : internalTicketData!;

  const headerTitle = currentTicket.title;
  const headerId = currentTicket.ticketId;

  const userStatus = isUser ? userTicket!.userTicketStatus : internalTicketData!.userTicketStatus;
  const internalStatus = !isUser ? internalTicketData?.status : undefined;

  const createdByName = isUser ? userTicket!.createdByName : internalTicketData!.createdByName;
  const assignedToName = isUser ? userTicket!.assignedToName : internalTicketData!.assignedToName;
  const createdAt = isUser ? userTicket!.createdAt : internalTicketData!.createdAt;
  const updatedAt = isUser ? userTicket!.updatedAt : internalTicketData!.updatedAt;

  const canAgentUpdate =
    isAgent &&
    internalTicketData!.assignedToUserId != null &&
    internalTicketData!.assignedToUserId === auth.userId;

  const showClarification =
    isUser &&
    !!userTicket &&
    (userTicket.userTicketStatus?.toUpperCase() === "WAITING FOR YOUR INPUT" ||
      Boolean(userTicket.clarificationPrompt));

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
      payload.newAssignedToUserId = newAssignedToUserId ?? null;
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
          onChange={(e) =>
            setNewAssignedToUserId(e.target.value === "" ? null : Number(e.target.value))
          }
          helperText="agents dropdown filtered by department."
          size="small"
        />
      );
    }

    if (overrideType === "STATUS") {
      return (
        <TextField
          select
          label="New Internal Status"
          value={newValue}
          onChange={(e) => setNewValue(e.target.value)}
          size="small"
        >
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
        <TextField
          select
          label="New Category"
          value={newValue}
          onChange={(e) => setNewValue(e.target.value)}
          size="small"
        >
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
        <TextField
          select
          label="New Priority"
          value={newValue}
          onChange={(e) => setNewValue(e.target.value)}
          size="small"
        >
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
          size="small"
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

    return (
      <TextField
        label="New Value"
        value={newValue}
        onChange={(e) => setNewValue(e.target.value)}
        multiline
        minRows={3}
        size="small"
      />
    );
  };

  //AI summary strip logic used in Band 2.
  const classificationFailed = Boolean(internalTicketData?.aiFailed);
  const classificationReady = Boolean(
    // internalTicketData?.aiCategory ||
    //   internalTicketData?.aiPriority ||
      internalTicketData?.aiTriagedAt
  );
  const vagueActive =
    internalTicketData?.status === "VAGUE" ||
    Boolean(internalTicketData?.vagueReason) ||
    Boolean(internalTicketData?.clarificationPrompt);
  const routingReady = Boolean(
    internalTicketData?.assignedToName || internalTicketData?.firstAssignedAt
  );

  /* 
  const duplicateState = (internalTicketData?.duplicateState ?? "NONE").toString().toUpperCase();
  const duplicatePartial = true; */

  const openAndScrollToJourney = () => {
    setAiExpanded(true);

    requestAnimationFrame(() => {
      setTimeout(() => {
        const el = aiJourneyRef.current;
        if (!el) return;
        const y = el.getBoundingClientRect().top + window.scrollY - APPBAR_SCROLL_OFFSET;
        window.scrollTo({ top: y, behavior: "smooth" });
      }, 140);
    });
  };

  return (
    <>
      <Stack spacing={1.35} sx={{ width: "100%" }}>
        {/* =========================
           Band 1: Header + Meta
           ========================= */}
        <Paper variant="outlined" sx={{ ...CARD_SX, p: 1.35 }}>
          <Stack spacing={1}>
            <Stack
              direction={{ xs: "column", lg: "row" }}
              justifyContent="space-between"
              alignItems={{ xs: "flex-start", lg: "flex-start" }}
              spacing={0.9}
            >
              <Box sx={{ minWidth: 0 }}>
                <Typography variant="h5" sx={{ fontWeight: 900, lineHeight: 1.15, mb: 0.1 }}>
                  {headerTitle}
                </Typography>

                <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
                  <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
                    Ticket #{headerId}
                  </Typography>

                  {!isUser && internalTicketData?.currentTextVersion != null && (
                    <Chip
                      size="small"
                      label={`Text v${internalTicketData.currentTextVersion}`}
                      sx={{
                        fontWeight: 800,
                        bgcolor: alpha("#219EBC", 0.12),
                        color: "primary.main",
                      }}
                    />
                  )}
                </Stack>
              </Box>

              <Stack direction="row" spacing={0.8} alignItems="center" flexWrap="wrap" useFlexGap>
                {!isUser && internalStatus && (
                  <Chip
                    label={`Internal: ${internalStatus}`}
                    size="small"
                    sx={{ ...statusChipSx(internalStatus), fontWeight: 800 }}
                  />
                )}
                <Chip
                  label={isUser ? userStatus : `User: ${userStatus}`}
                  size="small"
                  sx={{ ...statusChipSx(userStatus), fontWeight: 800 }}
                />
              </Stack>
            </Stack>

            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: {
                  xs: "1fr",
                  sm: "repeat(2, minmax(0, 1fr))",
                  xl: "repeat(4, minmax(0, 1fr))",
                },
                gap: 0.7,
              }}
            >
              <MetaPill label="Created" value={formatDateTime(createdAt)} />
              <MetaPill label="Updated" value={formatDateTime(updatedAt)} />
              <MetaPill label="Created By" value={createdByName ?? "—"} />
              <MetaPill label="Assigned To" value={assignedToName ?? "Unassigned"} />
            </Box>
          </Stack>
        </Paper>

        {isUser ? (
          <>
            <Paper variant="outlined" sx={CARD_SX}>
              <Typography sx={{ fontWeight: 900, mb: 0.4 }}>Description</Typography>
              <Typography sx={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>
                {userTicket!.description ?? ""}
              </Typography>
            </Paper>

            {showClarification && (
              <VagueClarificationPanel
                ticket={userTicket!}
                isSubmitting={clarifyMutation.isPending}
                onSubmit={async (body) => {
                  await clarifyMutation.mutateAsync(body);
                }}
              />
            )}

            <Box sx={{ width: "100%" }}>
              <TicketDetailsComments ticketId={idNum} role={auth.role ?? "USER"} />
            </Box>
          </>
        ) : (
          <>
            {/* =========================
               Band 2:
               Left = Description + compact AI summary
               Right = Current state + action
               ========================= */}
            <Box
              sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr", xl: "1.45fr 1fr" },
                gap: 1.35,
                alignItems: "start",
              }}
            >
              <Stack spacing={1.1}>
                <Paper variant="outlined" sx={CARD_SX}>
                  <Typography sx={{ fontWeight: 900, mb: 0.4 }}>Description</Typography>
                  <Typography sx={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>
                    {internalTicketData!.description ?? ""}
                  </Typography>
                </Paper>

                <Paper variant="outlined" sx={{ ...CARD_SX, p: 1.1 }}>
                  <Typography sx={{ fontWeight: 900, mb: 0.65 }}>AI Summary</Typography>

                  <Stack direction="row" spacing={0.7} flexWrap="wrap" useFlexGap sx={{ mb: 0.85 }}>
                    <SummaryChip
                      label={
                        classificationFailed
                          ? "Classification failed"
                          : classificationReady
                          ? "Classification completed"
                          : "Classification pending"
                      }
                      tone={
                        classificationFailed
                          ? "error"
                          : classificationReady
                          ? "success"
                          : "info"
                      }
                      icon={
                        classificationFailed ? <ErrorOutlineIcon /> : <CheckCircleOutlineIcon />
                      }
                    />
                    <SummaryChip
                      label={vagueActive ? "Needs clarification" : "Vague check complete"}
                      tone={vagueActive ? "warning" : "success"}
                      icon={vagueActive ? <HelpOutlineIcon /> : <CheckCircleOutlineIcon />}
                    />
                    <SummaryChip
                      label={routingReady ? "Routing completed" : "Routing pending"}
                      tone={routingReady ? "success" : "info"}
                      icon={routingReady ? <CheckCircleOutlineIcon /> : <AutorenewIcon />}
                    />
                    {/* <SummaryChip
                      label={
                        duplicateState !== "NONE"
                          ? `Duplicate: ${duplicateState}`
                          : "Duplicate partial"
                      }
                      tone={duplicatePartial ? "default" : "info"}
                      icon={<ContentCopyOutlinedIcon />}
                    /> */}
                  </Stack>

                  <Button variant="outlined" onClick={openAndScrollToJourney}>
                    View full AI journey
                  </Button>
                </Paper>
              </Stack>

              <Stack spacing={1.1}>
                <Paper variant="outlined" sx={{ ...CARD_SX, p: 1.1 }}>
                  <Typography sx={{ fontWeight: 900, mb: 0.65 }}>Current Ticket State</Typography>
                  <Stack spacing={0.6}>
                    <CompactInfoRow label="Category" value={internalTicketData?.aiCategory ?? "—"} />
                    <CompactInfoRow label="Priority" value={internalTicketData?.aiPriority ?? "—"} />
                    <CompactInfoRow
                      label="Duplicate"
                      value={(internalTicketData?.duplicateState ?? "NONE").toString()}
                    />
                    <CompactInfoRow
                      label="Assignee"
                      value={internalTicketData?.assignedToName ?? "Unassigned"}
                    />
                    <CompactInfoRow label="Internal Status" value={internalTicketData?.status ?? "—"} />
                  </Stack>
                </Paper>

                {isAdmin ? (
                  <Paper variant="outlined" sx={{ ...CARD_SX, p: 1.1 }}>
                    <Typography sx={{ fontWeight: 1000, mb: 0.3 }}>Admin Override</Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 0.9 }}>
                      Apply a manual override when human or operational judgment should take precedence.
                    </Typography>

                    <Stack spacing={1.1}>
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
                        size="small"
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
                        size="small"
                      />

                      <Button
                        variant="contained"
                        onClick={submitAdminOverride}
                        disabled={adminOverride.isPending}
                      >
                        {adminOverride.isPending ? "Applying..." : "Apply Override"}
                      </Button>

                      {adminOverride.isError && (
                        <Alert severity="error">Failed to apply override.</Alert>
                      )}
                      {adminOverride.isSuccess && (
                        <Alert severity="success">Override applied.</Alert>
                      )}
                    </Stack>
                  </Paper>
                ) : (
                  <Paper variant="outlined" sx={{ ...CARD_SX, p: 1.1 }}>
                    <Typography sx={{ fontWeight: 1000, mb: 0.3 }}>Agent Action</Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 0.9 }}>
                      Update workflow status for tickets assigned to you.
                    </Typography>

                    {!canAgentUpdate ? (
                      <Alert severity="info">
                        You can update status only for tickets assigned to you.
                      </Alert>
                    ) : (
                      <Stack spacing={0.8}>
                        <TextField
                          select
                          label="New Status"
                          value={agentStatus}
                          onChange={(e) =>
                            setAgentStatus(
                              e.target.value as UpdateTicketStatusRequestBean["status"]
                            )
                          }
                          size="small"
                        >
                          {AGENT_STATUS_OPTIONS.map((s) => (
                            <MenuItem key={s} value={s}>
                              {s}
                            </MenuItem>
                          ))}
                        </TextField>

                        <Button
                          variant="contained"
                          onClick={submitAgentStatus}
                          disabled={agentUpdate.isPending}
                        >
                          {agentUpdate.isPending ? "Updating..." : "Update Status"}
                        </Button>

                        {agentUpdate.isError && (
                          <Alert severity="error">Failed to update status.</Alert>
                        )}
                        {agentUpdate.isSuccess && (
                          <Alert severity="success">Status updated.</Alert>
                        )}
                      </Stack>
                    )}
                  </Paper>
                )}
              </Stack>
            </Box>

            {/* =========================
               Band 3: Expandable full AI journey
               ========================= */}
            <Box ref={aiJourneyRef}>
              <Accordion
                expanded={aiExpanded}
                onChange={(_, expanded) => setAiExpanded(expanded)}
                sx={{
                  "& .MuiAccordionSummary-root": {
                    backgroundColor: "rgba(2,48,71,0.03)",
                    color: "text.primary",
                    borderBottom: "1px solid rgba(2,48,71,0.07)",
                  },
                  "& .MuiAccordionSummary-root.Mui-expanded": {
                    backgroundColor: "rgba(2,48,71,0.05)",
                  },
                }}
              >
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography sx={{ fontWeight: 900 }}>AI Journey Details</Typography>
                </AccordionSummary>
                <AccordionDetails sx={{ p: 1.1 }}>
                  <AiAutomationPanel
                    ticket={internalTicketData!}
                    onOpenTextHistory={() => setHistoryOpen(true)}
                    hasTextHistory={(textHistoryQuery.data?.length ?? 0) > 0}
                    hideSectionHeader
                  />
                </AccordionDetails>
              </Accordion>
            </Box>

            <Box sx={{ width: "100%" }}>
              <TicketDetailsComments ticketId={idNum} role={auth.role ?? "USER"} />
            </Box>
          </>
        )}
      </Stack>

      {!isUser && (
        <Dialog
          open={historyOpen}
          onClose={() => setHistoryOpen(false)}
          maxWidth="md"
          fullWidth
          scroll="paper"
        >
          <DialogTitle sx={{ pr: 6, position: "relative" }}>
            Ticket Text Version History
            <IconButton
              aria-label="close"
              onClick={() => setHistoryOpen(false)}
              sx={{ position: "absolute", right: 10, top: 10, color: "inherit" }}
            >
              <CloseIcon />
            </IconButton>
          </DialogTitle>

          <DialogContent sx={{ px: 1.5, pt: 1.5, pb: 1.5 }}>
            {textHistoryQuery.isLoading ? (
              <LoadingSkeleton variant="list" count={3} />
            ) : (
              <TicketTextHistoryPanel versions={textHistoryQuery.data ?? []} />
            )}
          </DialogContent>
        </Dialog>
      )}
    </>
  );
}