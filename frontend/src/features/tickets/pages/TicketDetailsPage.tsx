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
import DuplicateReviewPanel from "../components/DuplicateReviewPanel";
import TicketDetailsComments from "../components/TicketDetailsComments";
import TicketTextHistoryPanel from "../components/TicketTextHistoryPanel";
import VagueClarificationPanel from "../components/VagueClarificationPanel";
import { formatDateTime } from "../../../utils/dateTime";
import KbSuggestionPanel from "../components/KbSuggestionPanel";
import KbArticleDialog from "../components/KbArticleDialog";
import GenerateKbDraftDialog from "../components/GenerateKbDraftDialog";
import GlobalSnackbar from "../../../components/GlobalSnackbar";

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
  useConfirmedDuplicates,
  useEligibleAgents,
  usePrimaryLink,
  useTicketDetailsInternal,
  useTicketDetailsUser,
  useTicketTextVersionHistory,
  useGenerateKbDraft,
  useManualKbSuggestion,
  useRespondToKbSuggestion,
  useTicketComments, 
} from "../hooks";

const AGENT_STATUS_OPTIONS: UpdateTicketStatusRequestBean["status"][] = [
  "IN_PROGRESS",
  "RESOLVED",
  "CLOSED",
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
const DUPLICATE_OVERRIDE_OPTIONS = ["NONE"];

/** Shared band/card styling */
const CARD_SX = {
  p: 1.35,
  borderRadius: 2,
  border: "1px solid rgba(2,48,71,0.10)",
  boxShadow: "0 2px 10px rgba(2,48,71,0.05)",
} as const;

/** App bar offset used when smooth-scrolling to Band 3 of AI Automation. */
const APPBAR_SCROLL_OFFSET = 88;

function getAllowedStatusOverrideOptions(ticket: TicketResponseBean): TicketStatus[] {
  const currentStatus = ticket.status;
  const isAssigned = ticket.assignedToUserId != null;

  switch (currentStatus) {
    case "VAGUE":
      return ["READY"];

    case "READY":
      return isAssigned ? ["IN_PROGRESS", "RESOLVED", "CLOSED"] : ["RESOLVED", "CLOSED"];

    case "IN_PROGRESS":
      return ["RESOLVED", "CLOSED"];

    case "RESOLVED":
      return ["CLOSED"];

    default:
      return [];
  }
}

function getAllowedOverrideTypes(ticket: TicketResponseBean): AdminOverrideType[] {
  const allowed: AdminOverrideType[] = ["CATEGORY", "PRIORITY", "KB_DRAFT"];

  const statusOptions = getAllowedStatusOverrideOptions(ticket);
  if (statusOptions.length > 0) {
    allowed.unshift("STATUS");
  }

  if (
    (ticket.duplicateState ?? "").toString().toUpperCase() === "CONFIRMED" &&
    ticket.status === "DUPLICATE"
  ) {
    allowed.push("DUPLICATE_LINK");
  }

  allowed.push("ASSIGNMENT");

  return allowed;
}

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
      bgcolor: alpha("#15803D", 0.1),
      color: "#15803D",
      border: "1px solid rgba(21,128,61,0.18)",
    },
    warning: {
      bgcolor: alpha("#FFB703", 0.2),
      color: "#875F00",
      border: "1px solid rgba(255,183,3,0.28)",
    },
    info: {
      bgcolor: alpha("#219EBC", 0.12),
      color: "#0E6B84",
      border: "1px solid rgba(33,158,188,0.18)",
    },
    error: {
      bgcolor: alpha("#C62828", 0.1),
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

  const userTicket = userQuery.data as UserTicketResponseBean | undefined;
  const internalTicket = internalQuery.data as TicketResponseBean | undefined;

  const duplicateState = (internalTicket?.duplicateState ?? "NONE").toString().toUpperCase();

  const clarifyMutation = useClarifyVagueTicket(idNum ?? -1);

  const agentUpdate = useAgentUpdateStatus(idNum ?? -1);
  const [agentStatus, setAgentStatus] =
    React.useState<UpdateTicketStatusRequestBean["status"]>("IN_PROGRESS");

  const adminOverride = useAdminOverride(idNum ?? -1);
  const [overrideType, setOverrideType] = React.useState<AdminOverrideType>("STATUS");
  const [newValue, setNewValue] = React.useState<string>("");
  const [newAssignedToUserId, setNewAssignedToUserId] = React.useState<number | null>(null);
  const [referenceTicketId, setReferenceTicketId] = React.useState<number | null>(null);
  const [reason, setReason] = React.useState<string>("");

  const [lastAdminAction, setLastAdminAction] = React.useState<
    "duplicate_review" | "admin_override" | null
  >(null);
  
  /** Band 3 AI journey accordion state to be expanded by default */
  const [aiExpanded, setAiExpanded] = React.useState(true);

  /** Dialog for text history which will be opened only from Vague Detection stage action. */
  const [historyOpen, setHistoryOpen] = React.useState(false);
  const [primaryLinkOpen, setPrimaryLinkOpen] = React.useState(false);
  const [confirmedDuplicatesOpen, setConfirmedDuplicatesOpen] = React.useState(false);

  const respondToKbSuggestionMutation = useRespondToKbSuggestion(idNum ?? -1);
  const generateKbDraftMutation = useGenerateKbDraft(idNum ?? -1);
  const manualKbSuggestionMutation = useManualKbSuggestion(idNum ?? -1);

  const commentsQuery = useTicketComments(
    idNum,
    Boolean(idNum && auth.token && !isUser)
  );

  const [kbArticleOpen, setKbArticleOpen] = React.useState(false);
  const [kbDraftDialogOpen, setKbDraftDialogOpen] = React.useState(false);
  const [selectedPublicCommentIds, setSelectedPublicCommentIds] = React.useState<number[]>([]);
  const [manualKbArticleId, setManualKbArticleId] = React.useState<number | null>(null);

  const [snackbarOpen, setSnackbarOpen] = React.useState(false);
  const [snackbarMessage, setSnackbarMessage] = React.useState("");
  const [snackbarSeverity, setSnackbarSeverity] = React.useState<
    "success" | "error" | "warning" | "info"
  >("info");

  const [draftPollingActive, setDraftPollingActive] = React.useState(false);

  /** Ref used so the Band 2 "View full journey" button expands and scrolls to Band 3. */
  const aiJourneyRef = React.useRef<HTMLDivElement | null>(null);

  const textHistoryQuery = useTicketTextVersionHistory(
    idNum,
    Boolean(idNum && auth.token && (isAdmin || isAgent))
  );

  const primaryLinkQuery = usePrimaryLink(
    idNum,
    Boolean(idNum && auth.token && !isUser && primaryLinkOpen && duplicateState === "CONFIRMED")
  );

  const confirmedDuplicatesQuery = useConfirmedDuplicates(
    idNum,
    Boolean(idNum && auth.token && !isUser && confirmedDuplicatesOpen)
  );

  const isLoading = isUser ? userQuery.isLoading : internalQuery.isLoading;

  const internalTicketData = !isUser ? internalTicket : undefined;

  const eligibleAgentsQuery = useEligibleAgents(
    idNum,
    Boolean(idNum && auth.token && isAdmin && overrideType === "ASSIGNMENT" && !isUser)
  );

  const allowedOverrideTypes = internalTicketData
    ? getAllowedOverrideTypes(internalTicketData)
    : [];

  const allowedStatusOptions = internalTicketData
    ? getAllowedStatusOverrideOptions(internalTicketData)
    : [];

  const canOverrideDuplicateLink =
    (internalTicketData?.duplicateState ?? "").toString().toUpperCase() === "CONFIRMED" &&
    internalTicketData?.status === "DUPLICATE";

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
      if (overrideType === "STATUS") {
        const nextOptions = getAllowedStatusOverrideOptions(internalTicket);
        setNewValue(nextOptions[0] ?? "");
      }

      if (overrideType === "CATEGORY") setNewValue(internalTicket.aiCategory ?? "");
      if (overrideType === "PRIORITY") setNewValue(internalTicket.aiPriority ?? "");

      if (overrideType === "DUPLICATE_LINK") {
        setNewValue("NONE");
        setReferenceTicketId(null);
      }

      if (overrideType === "ASSIGNMENT") {
        setNewAssignedToUserId(internalTicket.assignedToUserId ?? null);
      }
    }
  }, [internalTicket, isAgent, isAdmin, overrideType]);

  React.useEffect(() => {
    if (!isAdmin || !internalTicketData) return;

    if (!allowedOverrideTypes.includes(overrideType)) {
      setOverrideType(allowedOverrideTypes[0] ?? "CATEGORY");
      setNewValue("");
      setNewAssignedToUserId(null);
      setReason("");
      setLastAdminAction(null);
    }
  }, [isAdmin, internalTicketData, overrideType, allowedOverrideTypes]);

  React.useEffect(() => {
    if (
      isAdmin &&
      internalTicket?.duplicateState === "POTENTIAL" &&
      referenceTicketId == null &&
      internalTicket.primaryTicketId != null
    ) {
      setReferenceTicketId(internalTicket.primaryTicketId);
    }
  }, [
    isAdmin,
    internalTicket?.duplicateState,
    internalTicket?.primaryTicketId,
    referenceTicketId,
  ]);

  React.useEffect(() => {
    if (!draftPollingActive || !idNum || !auth.token || isUser) return;

    let attempts = 0;
    const maxAttempts = 10;

    const interval = window.setInterval(async () => {
      attempts += 1;
      const refreshedResult = await internalQuery.refetch();
      const refreshed = refreshedResult.data;
      const hasDraft = Boolean(refreshed?.kbDraftExists || refreshed?.draftKbId);

      if (hasDraft) {
        window.clearInterval(interval);
        setDraftPollingActive(false);
        openSnackbar("KB draft is now available.", "success");
      } else if (attempts >= maxAttempts) {
        window.clearInterval(interval);
        setDraftPollingActive(false);
        openSnackbar("KB draft generation is still processing. Please refresh later.", "info");
      }
    }, 3000);

    return () => window.clearInterval(interval);
  }, [draftPollingActive, idNum, auth.token, isUser, internalQuery]);

  if (isLoading) return <LoadingSkeleton variant="detail" />;
  if (!idNum) return <Typography>Invalid ticket id.</Typography>;

  if (isUser && !userTicket) return <Typography>Ticket not found.</Typography>;
  if (!isUser && !internalTicket) return <Typography>Ticket not found.</Typography>;

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

  const userKbSuggestionPending =
  isUser &&
  userTicket?.kbSuggestionStatus?.toString().toUpperCase() === "SUGGESTED" &&
  Boolean(userTicket?.suggestedKbId);

  const showClarification =
    isUser &&
    !!userTicket &&
    !userKbSuggestionPending &&
    (Boolean(userTicket?.clarificationPrompt) || Boolean(userTicket?.vagueReason));

  const canGenerateKbDraft =
    isAgent &&
    canAgentUpdate &&
    (internalTicketData?.status === "RESOLVED" || internalTicketData?.status === "CLOSED") &&
    !internalTicketData?.kbDraftExists;

  const canOpenExistingKbDraft =
    !isUser && Boolean(internalTicketData?.kbDraftExists && internalTicketData?.draftKbId);

  const canManualSuggestKb =
    isAgent &&
    canAgentUpdate &&
    internalTicketData?.status !== "VAGUE" &&
    internalTicketData?.status !== "DUPLICATE_REVIEW" &&
    internalTicketData?.status !== "DUPLICATE" &&
    internalTicketData?.status !== "KB_SUGGESTED" &&
    duplicateState !== "CONFIRMED";

  const openSnackbar = (
    message: string,
    severity: "success" | "error" | "warning" | "info" = "info"
  ) => {
    setSnackbarMessage(message);
    setSnackbarSeverity(severity);
    setSnackbarOpen(true);
  };

  const submitAgentStatus = async () => {
    if (!canAgentUpdate || !idNum) return;
    await agentUpdate.mutateAsync({ status: agentStatus });
  };

  const submitAdminOverride = async () => {
    if (!isAdmin || !idNum) return;

    if (overrideType === "DUPLICATE_LINK") {
      const isAllowedDuplicateOverride =
        (internalTicketData?.duplicateState ?? "").toString().toUpperCase() === "CONFIRMED" &&
        internalTicketData?.status === "DUPLICATE" &&
        newValue === "NONE";

      if (!isAllowedDuplicateOverride) {
        return;
      }
    }

    setLastAdminAction("admin_override");

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

  const submitConfirmDuplicate = async () => {
    if (!isAdmin || !idNum || referenceTicketId == null) return;

    setLastAdminAction("duplicate_review");

    const payload: AdminOverrideRequestBean = {
      overrideType: "DUPLICATE_LINK",
      newValue: "CONFIRMED",
      referenceTicketId,
      reason: reason.trim() || null,
    };

    await adminOverride.mutateAsync(payload);
    setReason("");
    setReferenceTicketId(null);
  };

  const submitMarkNotDuplicate = async () => {
    if (!isAdmin || !idNum) return;

    setLastAdminAction("duplicate_review");

    const payload: AdminOverrideRequestBean = {
      overrideType: "DUPLICATE_LINK",
      newValue: "NONE",
      reason: reason.trim() || null,
    };

    await adminOverride.mutateAsync(payload);
    setReason("");
    setReferenceTicketId(null);
  };

  const submitKbSuggestionAccept = async () => {
    if (!isUser || !idNum) return;
    await respondToKbSuggestionMutation.mutateAsync({ action: "ACCEPTED" });
    openSnackbar("Thanks. The suggested article was marked as helpful.", "success");
  };

  const submitKbSuggestionReject = async () => {
    if (!isUser || !idNum) return;
    await respondToKbSuggestionMutation.mutateAsync({ action: "REJECTED" });
    openSnackbar("Thanks. The ticket will continue for further handling.", "info");
  };

  const submitGenerateKbDraft = async () => {
    if (!isAgent || !idNum || selectedPublicCommentIds.length === 0) return;

    const res = await generateKbDraftMutation.mutateAsync({
      selectedCommentIds: selectedPublicCommentIds,
    });

    setKbDraftDialogOpen(false);
    setSelectedPublicCommentIds([]);

    if (res?.kbDraftExists || res?.draftKbId) {
      openSnackbar("KB draft generated successfully.", "success");
    } else {
      openSnackbar(
        "KB draft generation was requested. It may still be processing.",
        "info"
      );
      setDraftPollingActive(true);
    }
  };

  const submitManualKbSuggestion = async () => {
    if (!isAgent || !idNum || manualKbArticleId == null) return;

    await manualKbSuggestionMutation.mutateAsync({
      kbId: manualKbArticleId,
    });

    setManualKbArticleId(null);
    openSnackbar("KB article was suggested to the user.", "success");
  };

  const renderOverrideNewValueField = () => {
    if (overrideType === "ASSIGNMENT") {
      return (
        <TextField
          select
          label="Assign to Eligible Agent"
          value={newAssignedToUserId ?? ""}
          onChange={(e) =>
            setNewAssignedToUserId(e.target.value === "" ? null : Number(e.target.value))
          }
          size="small"
          helperText="Only eligible agents for this ticket are shown."
          disabled={eligibleAgentsQuery.isLoading}
        >
          <MenuItem value="">—</MenuItem>
          {(eligibleAgentsQuery.data ?? []).map((agent) => (
            <MenuItem key={agent.userId} value={agent.userId}>
              {agent.username} ({agent.email})
            </MenuItem>
          ))}
        </TextField>
      );
    }

    if (overrideType === "STATUS") {
      if (allowedStatusOptions.length === 0) {
        return (
          <Alert severity="info">
            No valid status override transitions are available for this ticket.
          </Alert>
        );
      }

      return (
        <TextField
          select
          label="New Internal Status"
          value={newValue}
          onChange={(e) => setNewValue(e.target.value)}
          size="small"
        >
          {allowedStatusOptions.map((s) => (
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
      if (!canOverrideDuplicateLink) {
        return (
          <Alert severity="info">
            Duplicate override is available here only for reverting a confirmed duplicate back to
            NONE when the ticket is currently in DUPLICATE status.
          </Alert>
        );
      }

      return (
        <TextField
          select
          label="New Duplicate State"
          value={newValue}
          onChange={(e) => setNewValue(e.target.value)}
          size="small"
          helperText="Only CONFIRMED → NONE override is supported through this panel."
        >
          {DUPLICATE_OVERRIDE_OPTIONS.map((d) => (
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
  const classificationReady = Boolean(internalTicketData?.aiTriagedAt);
  const vagueActive =
    internalTicketData?.status === "VAGUE" ||
    Boolean(internalTicketData?.vagueReason) ||
    Boolean(internalTicketData?.clarificationPrompt);

  const kbSuggestionBlockingRouting =
    internalTicketData?.kbSuggestionStatus?.toString().toUpperCase() === "SUGGESTED" &&
    internalTicketData?.status === "KB_SUGGESTED";

  const routingSummaryLabel =
    duplicateState === "CONFIRMED"
      ? "Routing skipped"
      : duplicateState === "POTENTIAL" && internalTicketData?.status === "DUPLICATE_REVIEW"
      ? "Routing waiting on review"
      : kbSuggestionBlockingRouting
      ? "Routing waiting on user"
      : internalTicketData?.assignedToName || internalTicketData?.firstAssignedAt
      ? "Routing completed"
      : "Routing pending";

  const routingSummaryTone =
    duplicateState === "CONFIRMED"
      ? "default"
      : duplicateState === "POTENTIAL" && internalTicketData?.status === "DUPLICATE_REVIEW"
      ? "warning"
      : kbSuggestionBlockingRouting
      ? "warning"
      : internalTicketData?.assignedToName || internalTicketData?.firstAssignedAt
      ? "success"
      : "info";

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

            {userKbSuggestionPending && (
              <KbSuggestionPanel
                title={userTicket?.suggestedKbTitle}
                preview={userTicket?.suggestedKbPreview}
                isSubmitting={respondToKbSuggestionMutation.isPending}
                onViewArticle={() => setKbArticleOpen(true)}
                onAccept={submitKbSuggestionAccept}
                onReject={submitKbSuggestionReject}
                isError={respondToKbSuggestionMutation.isError}
              />
            )}

            <Box sx={{ width: "100%" }}>
              <TicketDetailsComments ticketId={idNum} role={auth.role ?? "USER"} />
            </Box>
          </>
        ) : (
          <>
            <Paper variant="outlined" sx={CARD_SX}>
              <Typography sx={{ fontWeight: 900, mb: 0.4 }}>Description</Typography>
              <Typography sx={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>
                {internalTicketData!.description ?? ""}
              </Typography>
            </Paper>

            {isAdmin && duplicateState === "POTENTIAL" && (
              <DuplicateReviewPanel
                referenceTicketId={referenceTicketId}
                suggestedPrimaryTicketId={internalTicketData?.primaryTicketId ?? null}
                suggestedPrimaryTicketTitle={internalTicketData?.primaryTicketTitle ?? null}
                onReferenceTicketIdChange={setReferenceTicketId}
                reason={reason}
                onReasonChange={setReason}
                onConfirmDuplicate={submitConfirmDuplicate}
                onMarkNotDuplicate={submitMarkNotDuplicate}
                isSubmitting={adminOverride.isPending}
                isError={adminOverride.isError && lastAdminAction === "duplicate_review"}
                isSuccess={adminOverride.isSuccess && lastAdminAction === "duplicate_review"}
              />
            )}
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
                      icon={classificationFailed ? <ErrorOutlineIcon /> : <CheckCircleOutlineIcon />}
                    />

                    <SummaryChip
                      label={vagueActive ? "Needs clarification" : "Vague check complete"}
                      tone={vagueActive ? "warning" : "success"}
                      icon={vagueActive ? <HelpOutlineIcon /> : <CheckCircleOutlineIcon />}
                    />

                    <SummaryChip
                      label={
                        duplicateState === "CONFIRMED"
                          ? "Confirmed duplicate"
                          : duplicateState === "POTENTIAL"
                          ? "Potential duplicate"
                          : "No duplicate found"
                      }
                      tone={
                        duplicateState === "CONFIRMED"
                          ? "info"
                          : duplicateState === "POTENTIAL"
                          ? "warning"
                          : "success"
                      }
                      icon={<ContentCopyOutlinedIcon />}
                    />

                    <SummaryChip
                      label={
                        internalTicketData?.kbSuggestionStatus?.toString().toUpperCase() === "SUGGESTED"
                          ? "KB waiting on user"
                          : internalTicketData?.kbSuggestionStatus?.toString().toUpperCase() === "ACCEPTED"
                          ? "KB accepted"
                          : internalTicketData?.kbSuggestionStatus?.toString().toUpperCase() === "REJECTED"
                          ? "KB rejected"
                          : internalTicketData?.suggestedKbId
                          ? "KB suggested"
                          : duplicateState === "CONFIRMED"
                          ? "KB skipped"
                          : "KB pending / none"
                      }
                      tone={
                        internalTicketData?.kbSuggestionStatus?.toString().toUpperCase() === "SUGGESTED"
                          ? "warning"
                          : internalTicketData?.kbSuggestionStatus?.toString().toUpperCase() === "ACCEPTED"
                          ? "success"
                          : internalTicketData?.kbSuggestionStatus?.toString().toUpperCase() === "REJECTED"
                          ? "info"
                          : internalTicketData?.suggestedKbId
                          ? "info"
                          : duplicateState === "CONFIRMED"
                          ? "default"
                          : "info"
                      }
                      icon={<AutorenewIcon />}
                    />

                    <SummaryChip
                      label={routingSummaryLabel}
                      tone={routingSummaryTone}
                      icon={
                        routingSummaryTone === "success" ? (
                          <CheckCircleOutlineIcon />
                        ) : routingSummaryTone === "warning" ? (
                          <HelpOutlineIcon />
                        ) : (
                          <AutorenewIcon />
                        )
                      }
                    />
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
                    <CompactInfoRow label="Duplicate State" value={duplicateState} />
                    <CompactInfoRow
                      label="Primary Ticket"
                      value={
                        internalTicketData?.primaryTicketId
                          ? `${internalTicketData.primaryTicketTitle ?? "Ticket"} (Ticket #${internalTicketData.primaryTicketId})`
                          : "Not linked"
                      }
                    />
                    <CompactInfoRow
                      label="Assignee"
                      value={internalTicketData?.assignedToName ?? "Unassigned"}
                    />
                    <CompactInfoRow
                      label="Internal Status"
                      value={internalTicketData?.status ?? "—"}
                    />
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
                          setReferenceTicketId(null);
                          setReason("");
                          setLastAdminAction(null);
                        }}
                        size="small"
                      >
                        {allowedOverrideTypes.map((t) => (
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
                        disabled={
                          adminOverride.isPending ||
                          (overrideType === "STATUS" && allowedStatusOptions.length === 0) ||
                          (overrideType === "DUPLICATE_LINK" &&
                            !(
                              (internalTicketData?.duplicateState ?? "").toString().toUpperCase() ===
                                "CONFIRMED" &&
                              internalTicketData?.status === "DUPLICATE" &&
                              newValue === "NONE"
                            )) ||
                          (overrideType === "ASSIGNMENT" &&
                            (eligibleAgentsQuery.isLoading || newAssignedToUserId == null))
                        }
                      >
                        {adminOverride.isPending ? "Applying..." : "Apply Override"}
                      </Button>

                      {adminOverride.isError && lastAdminAction === "admin_override" && (
                        <Alert severity="error">Failed to apply override.</Alert>
                      )}
                      {adminOverride.isSuccess && lastAdminAction === "admin_override" && (
                        <Alert severity="success">Override applied.</Alert>
                      )}
                    </Stack>
                  </Paper>
                ) : (
                  <>
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

                    {canAgentUpdate && (
                      <Paper variant="outlined" sx={{ ...CARD_SX, p: 1.1 }}>
                        <Typography sx={{ fontWeight: 1000, mb: 0.3 }}>
                          Manual KB Suggestion
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 0.9 }}>
                          Suggest an existing KB article directly to the user when you know a
                          relevant article that can help resolve the issue.
                        </Typography>

                        {!canManualSuggestKb ? (
                          <Alert severity="info">
                            Manual KB suggestion is not available for the current ticket state.
                          </Alert>
                        ) : (
                          <Stack spacing={0.8}>
                            <TextField
                              label="KB Article ID"
                              value={manualKbArticleId ?? ""}
                              onChange={(e) =>
                                setManualKbArticleId(
                                  e.target.value === "" ? null : Number(e.target.value)
                                )
                              }
                              size="small"
                              inputProps={{
                                inputMode: "numeric",
                                pattern: "[0-9]*",
                              }}
                              helperText="Enter the existing KB article id to suggest to the user."
                            />

                            <Button
                              variant="outlined"
                              onClick={submitManualKbSuggestion}
                              disabled={
                                manualKbSuggestionMutation.isPending || manualKbArticleId == null
                              }
                            >
                              {manualKbSuggestionMutation.isPending
                                ? "Suggesting..."
                                : "Suggest KB Article"}
                            </Button>

                            {manualKbSuggestionMutation.isError && (
                              <Alert severity="error">
                                Failed to suggest KB article to the user.
                              </Alert>
                            )}
                          </Stack>
                        )}
                      </Paper>
                    )}

                    {isAgent && (canGenerateKbDraft || canOpenExistingKbDraft) && (
                      <Paper variant="outlined" sx={{ ...CARD_SX, p: 1.1 }}>
                        <Typography sx={{ fontWeight: 1000, mb: 0.3 }}>KB Drafting</Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 0.9 }}>
                          Generate or continue a knowledge base draft from the resolved ticket and
                          its public resolution comments.
                        </Typography>

                        <Stack spacing={0.8}>
                          {internalTicketData?.kbDraftExists ? (
                            <>
                              <CompactInfoRow
                                label="Draft"
                                value={
                                  internalTicketData?.draftKbId
                                    ? `${internalTicketData?.draftKbTitle ?? "KB Draft"} (KB #${internalTicketData.draftKbId})`
                                    : "Draft exists"
                                }
                              />
                              <CompactInfoRow
                                label="Status"
                                value={internalTicketData?.draftKbStatus ?? "—"}
                              />
                              <Button variant="outlined" disabled>
                                View / Edit Draft
                              </Button>
                            </>
                          ) : (
                            <Button
                              variant="contained"
                              onClick={() => setKbDraftDialogOpen(true)}
                              disabled={!canGenerateKbDraft}
                            >
                              Generate KB Draft
                            </Button>
                          )}
                        </Stack>
                      </Paper>
                    )}
                  </>
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
                    onOpenPrimaryLink={() => setPrimaryLinkOpen(true)}
                    onOpenConfirmedDuplicates={() => setConfirmedDuplicatesOpen(true)}
                    disablePrimaryLinkAction={duplicateState !== "CONFIRMED"}
                    disableConfirmedDuplicatesAction={false}
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

      <KbArticleDialog
        kbId={
          isUser
            ? userTicket?.suggestedKbId ?? null
            : internalTicketData?.suggestedKbId ?? null
        }
        open={kbArticleOpen}
        onClose={() => setKbArticleOpen(false)}
        titleOverride="Suggested Knowledge Base Article"
      />

      {!isUser && (
        <>
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

          <Dialog
            open={primaryLinkOpen}
            onClose={() => setPrimaryLinkOpen(false)}
            maxWidth="sm"
            fullWidth
            scroll="paper"
          >
            <DialogTitle sx={{ pr: 6, position: "relative" }}>
              Primary Ticket Link
              <IconButton
                aria-label="close"
                onClick={() => setPrimaryLinkOpen(false)}
                sx={{ position: "absolute", right: 10, top: 10, color: "inherit" }}
              >
                <CloseIcon />
              </IconButton>
            </DialogTitle>

            <DialogContent sx={{ px: 1.5, pt: 1.5, pb: 1.5 }}>
              {primaryLinkQuery.isLoading ? (
                <LoadingSkeleton variant="list" count={1} />
              ) : primaryLinkQuery.isError ? (
                <Alert severity="error">Failed to load primary ticket link.</Alert>
              ) : primaryLinkQuery.data ? (
                <Stack spacing={0.7}>
                  <Paper variant="outlined" sx={{ p: 1.15, borderRadius: 2 }}>
                    <Stack spacing={0.6}>
                      <CompactInfoRow
                        label="Primary Ticket"
                        value={`${primaryLinkQuery.data.primaryTicketTitle} (Ticket #${primaryLinkQuery.data.primaryTicketId})`}
                      />
                      <CompactInfoRow
                        label="Internal Status"
                        value={primaryLinkQuery.data.primaryInternalStatus}
                      />
                      <CompactInfoRow
                        label="User Status"
                        value={primaryLinkQuery.data.primaryUserTicketStatus}
                      />
                      <CompactInfoRow
                        label="Assigned Agent"
                        value={primaryLinkQuery.data.assignedAgentName ?? "Unassigned"}
                      />
                      <CompactInfoRow
                        label="Link Status"
                        value={primaryLinkQuery.data.linkStatus ?? "—"}
                      />
                      <CompactInfoRow
                        label="Duplicate Type"
                        value={primaryLinkQuery.data.duplicateType ?? "—"}
                      />
                    </Stack>
                  </Paper>
                </Stack>
              ) : (
                <Alert severity="info">No primary ticket link found.</Alert>
              )}
            </DialogContent>
          </Dialog>

          <Dialog
            open={confirmedDuplicatesOpen}
            onClose={() => setConfirmedDuplicatesOpen(false)}
            maxWidth="md"
            fullWidth
            scroll="paper"
          >
            <DialogTitle sx={{ pr: 6, position: "relative" }}>
              Confirmed Duplicate Tickets
              <IconButton
                aria-label="close"
                onClick={() => setConfirmedDuplicatesOpen(false)}
                sx={{ position: "absolute", right: 10, top: 10, color: "inherit" }}
              >
                <CloseIcon />
              </IconButton>
            </DialogTitle>

            <DialogContent sx={{ px: 1.5, pt: 1.5, pb: 1.5 }}>
              {confirmedDuplicatesQuery.isLoading ? (
                <LoadingSkeleton variant="list" count={2} />
              ) : confirmedDuplicatesQuery.isError ? (
                <Alert severity="error">Failed to load confirmed duplicates.</Alert>
              ) : (confirmedDuplicatesQuery.data?.length ?? 0) === 0 ? (
                <Alert severity="info">
                  No confirmed duplicate tickets are currently linked to this ticket.
                </Alert>
              ) : (
                <Stack spacing={0.75}>
                  {confirmedDuplicatesQuery.data!.map((dup) => (
                    <Paper key={dup.ticketId} variant="outlined" sx={{ p: 1, borderRadius: 1.5 }}>
                      <Stack spacing={0.4}>
                        <Typography sx={{ fontWeight: 800 }}>
                          {dup.title} (Ticket #{dup.ticketId})
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Created by {dup.createdByName} • {formatDateTime(dup.createdAt)}
                        </Typography>

                        <Stack direction="row" spacing={0.7} flexWrap="wrap" useFlexGap>
                          <Chip
                            label={`Internal: ${dup.internalStatus}`}
                            size="small"
                            sx={{ ...statusChipSx(dup.internalStatus), fontWeight: 700 }}
                          />
                          <Chip
                            label={`User: ${dup.userTicketStatus}`}
                            size="small"
                            sx={{ ...statusChipSx(dup.userTicketStatus), fontWeight: 700 }}
                          />
                        </Stack>
                      </Stack>
                    </Paper>
                  ))}
                </Stack>
              )}
            </DialogContent>
          </Dialog>

          <GenerateKbDraftDialog
            open={kbDraftDialogOpen}
            onClose={() => setKbDraftDialogOpen(false)}
            comments={commentsQuery.data ?? []}
            selectedIds={selectedPublicCommentIds}
            onSelectedIdsChange={setSelectedPublicCommentIds}
            onGenerate={submitGenerateKbDraft}
            isSubmitting={generateKbDraftMutation.isPending}
            isError={generateKbDraftMutation.isError}
          />
        </>
      )}

      <GlobalSnackbar
        open={snackbarOpen}
        message={snackbarMessage}
        severity={snackbarSeverity}
        onClose={() => setSnackbarOpen(false)}
      />
    </>
  );
}