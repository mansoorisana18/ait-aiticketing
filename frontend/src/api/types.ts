export type ApiValidationError = { field: string; message: string };

export type ApiSuccess<T> = {
  success: true;
  message?: string;
  data: T;
};

export type ApiFailure = {
  success: false;
  message: string;
  errors?: ApiValidationError[];
};

export type ApiResponse<T> = ApiSuccess<T> | ApiFailure;

export type UserRole = "USER" | "AGENT" | "ADMIN";

export type Department =
  | "TECHNICAL SUPPORT"
  | "BILLING AND PAYMENTS"
  | "ORDERS AND RETURNS"
  | "SALES AND PRESALES"
  | "ACCOUNT AND ACCESS"
  | "GENERAL INQUIRY";

export const DEPARTMENTS: Department[] = [
  "TECHNICAL SUPPORT",
  "BILLING AND PAYMENTS",
  "ORDERS AND RETURNS",
  "SALES AND PRESALES",
  "ACCOUNT AND ACCESS",
  "GENERAL INQUIRY",
];

export type TicketStatus =
  | "NEW"
  | "AI_PROCESSING"
  | "VAGUE"
  | "KB_SUGGESTED"
  | "READY"
  | "DUPLICATE_REVIEW"
  | "IN_PROGRESS"
  | "DUPLICATE"
  | "RESOLVED"
  | "CLOSED";

export type DuplicateState = "NONE" | "POTENTIAL" | "CONFIRMED";

export type KbSuggestionStatus = "SUGGESTED" | "ACCEPTED" | "REJECTED";

export type KbArticleStatus = "DRAFT" | "IN_REVIEW" | "PUBLISHED" | "REJECTED" | string;

export type AdminOverrideType =
  | "CATEGORY"
  | "PRIORITY"
  | "DUPLICATE_LINK"
  | "STATUS"
  | "KB_DRAFT"
  | "ASSIGNMENT";

export type LoginResponseBean = {
  userId: number;
  email: string;
  name: string;
  role: UserRole;
  token: string; //JWT access token returned by backend
};

export type UserResponseBean = {
  userId: number;
  email: string;
  name: string;
  role: UserRole;
  department?: Department | null;
};

//USER view ticket bean - only includes fields relevant to the user
export type UserTicketResponseBean = {
  ticketId: number;
  title: string;
  description: string;
  userTicketStatus: string;
  createdAt?: string | null;
  updatedAt?: string | null;
  createdByUserId?: number;
  createdByName?: string;
  createdByEmail?: string;
  assignedToName?: string | null;

  //for vague indication on user side
  vagueReason?: string | null;
  clarificationPrompt?: string | null;

  //KB suggestion
  suggestedKbId?: number | null;
  suggestedKbTitle?: string | null;
  suggestedKbPreview?: string | null;
  kbSuggestionStatus?: KbSuggestionStatus | string | null;
};

//ADMIN/AGENT view ticket bean - includes all fields, including those relevant to the agent/admin
export type TicketResponseBean = {
  ticketId: number;
  title: string;
  description: string;
  status: TicketStatus; //intenal status used by backend
  userTicketStatus: string; //ticket status as seen by user
  aiCategory?: string | null;
  aiPriority?: string | null;
  aiConfidence?: string | number | null;
  currentTextVersion?: number | null;

  //AI: DUPLICATE DETECTION fields
  duplicateState?: DuplicateState | string | null;
  duplicateReason?: string | null;
  duplicateConfidence?: number | null;
  duplicateSimilarity?: number | null;
  primaryTicketId?: number | null;
  primaryTicketTitle?: string | null;
  duplicateLinkType?: string | null;
  duplicateLinkStatus?: string | null;
  propagateResolution?: boolean | null;
  
  //AI: TRAIGE & ROUTING detail fields
  aiFailed?: boolean | null; //only for TRIAGE
  aiLastError?: string | null;
  aiTriagedAt?: string | null;
  vagueCount?: number | null; //no of times ticket has been marked as vague, used for both agent/admin and user view
  lastVagueAt?: string | null;
  firstAssignedAt?: string | null; //metrics field for dashboard
  vagueReason?: string | null;
  clarificationPrompt?: string | null;

  //KB suggestion preview/internal details
  suggestedKbId?: number | null;
  suggestedKbTitle?: string | null;
  suggestedKbPreview?: string | null;
  kbSuggestionStatus?: KbSuggestionStatus | string | null;
  kbSuggestionSource?: string | null;
  suggestedKbSimilarity?: number | null;

  //KB draft summary
  draftKbId?: number | null;
  draftKbTitle?: string | null;
  draftKbStatus?: KbArticleStatus | string | null;
  kbDraftExists?: boolean | null;
  kbDraftAiGenerated?: boolean | null;
  draftKbUpdatedAt?: string | null;

  createdAt?: string | null;
  updatedAt?: string | null;
  createdByUserId: number;
  createdByName: string;
  createdByEmail: string;
  assignedToUserId?: number | null;
  assignedToName?: string | null;
  assignedToEmail?: string | null;
};

//ticket comments
export type CommentVisibility = "PUBLIC" | "INTERNAL";

export type TicketCommentRequestBean = {
  body: string;
  visibility: CommentVisibility;
};

export type TicketCommentResponseBean = {
  commentId: number;
  ticketId: number;
  body: string;
  visibility: CommentVisibility;
  createdAt?: string | null;
  authorUserId: number;
  authorName: string;
  authorEmail: string;
};

//Admin ticket override
export type AdminOverrideRequestBean = {
  overrideType: AdminOverrideType;
  newValue?: string | null; //used for STATUS/CATEGORY/PRIORITY/DUPLICATE_LINK/KB_DRAFT
  newAssignedToUserId?: number | null; //used for ASSIGNMENT
  referenceTicketId?: number | null; //used for DUPLICATE_LINK to link to the ticket that this ticket is a duplicate of
  reason?: string | null;
};

export type AdminOverrideResponseBean = {
  overrideId: number;
  ticketId: number;
  overrideType: AdminOverrideType;
  oldValue?: string | null;
  newValue?: string | null;
  reason?: string | null;
  createdAt?: string | null;
  overriddenByUserId: number;
  overriddenByName: string;
  overriddenByEmail: string;
};

//Agent Ticket Status Update
export type UpdateTicketStatusRequestBean = {
  status: "IN_PROGRESS" | "RESOLVED" | "CLOSED";
};

//update user to agent request bean
export type PromoteToAgentRequestBean = {
  department: Department;
};

//Request bean for updating vague ticket with user clarification
export type UpdateVagueTicketRequestBean = {
  title?: string;
  clarificationAnswer: string;
};

//for vague history
export type TicketTextVersionResponseBean = {
  versionId: number;
  ticketId: number;
  versionNo: number;
  title: string;
  description: string;
  createdByUserId: number;
  createdAt?: string | null;
}

//for eligible agents in routing
export type EligibleAgentResponseBean = {
  userId: number;
  username: string;
  email: string;
  department: string;
};

//GetConfirmedDuplicates of a primary ticket visible to Admin/Agent
export type ConfirmedDuplicateTicketResponseBean = {
  ticketId: number;
  title: string;
  createdByUserId: number;
  createdByName: string;
  createdByEmail: string;
  internalStatus: string;
  userTicketStatus: string;
  createdAt?: string | null;
  propagateResolution?: boolean | null;
};

//GetPrimaryLink of a confirmed primary linked ticket of the given duplicate ticket
export type PrimaryLinkedTicketResponseBean = {
  primaryTicketId: number;
  primaryTicketTitle: string;
  primaryInternalStatus: string;
  primaryUserTicketStatus: string;
  assignedAgentUserId?: number | null;
  assignedAgentName?: string | null;
  assignedAgentEmail?: string | null;
  duplicateType?: string | null;
  linkStatus?: string | null;
  propagateResolution?: boolean | null;
};

//KB request beans
export type KbSuggestionResponseRequestBean = {
  action: "ACCEPTED" | "REJECTED";
};

export type ManualKbSuggestionRequestBean = {
  kbId: number;
};

export type GenerateKbDraftRequestBean = {
  selectedCommentIds: number[];
};

export type CreateKbArticleRequestBean = {
  title: string;
  body: string;
};

export type UpdateKbArticleRequestBean = {
  title: string;
  body: string;
};

export type UpdateKbDraftRequestBean = {
  title: string;
  body: string;
};

export type KbReviewDecisionRequestBean = {
  action: "APPROVE" | "REJECT";
};

//KB Response beans 
export type KbArticleResponseBean = {
  kbId: number;
  title: string;
  body: string;
  status: KbArticleStatus;

  isAiGenerated?: boolean | null;

  sourceTicketId?: number | null;

  createdByUserId?: number | null;
  createdByName?: string | null;
  createdByEmail?: string | null;

  lastModifiedByUserId?: number | null;
  lastModifiedByName?: string | null;
  lastModifiedByEmail?: string | null;

  approvedByUserId?: number | null;
  approvedByName?: string | null;
  approvedByEmail?: string | null;

  createdAt?: string | null;
  updatedAt?: string | null;
  agentSubmittedAt?: string | null;
  approvedAt?: string | null;
};

//Ticket summary metrics for admin & agent tickt pages
export type TicketSummaryMetricsResponseBean = {
  totalTickets: number;
  newCount: number;
  aiProcessingCount: number;
  vagueCount: number;
  readyCount: number;
  inProgressCount: number;
  resolvedCount: number;
  closedCount: number;
  assignedCount: number;
  unassignedCount: number;
  highPriorityCount: number;
  urgentPriorityCount: number;
  duplicateReviewCount: number;
  duplicateCount: number;
};

//AI summary metrics for admin analytics page
export type TriageMetricsResponseBean = {
  totalTicketsCreated: number;
  triageCompletedCount: number;
  triageSuccessRate: number | null;
  averageTriageTimeSeconds: number | null;
  vagueRate: number | null;
  averageAiConfidence: number | null;
  manualTriageOverrideRate: number | null;
  aiTriageAccuracy: number | null;
};

export type RoutingMetricsResponseBean = {
  routingAttempts: number;
  autoRoutingSuccessRate: number | null;
  noEligibleAgentRate: number | null;
  averageTimeToAssignmentFromTriageSeconds: number | null;
  assignmentOverrideRate: number | null;
  autoRoutingAccuracy: number | null;
};

export type DuplicateMetricsResponseBean = {
  duplicateChecksAttempted: number;
  duplicateCheckSuccessRate: number | null;
  averageDuplicateCheckTimeSeconds: number | null;
  autoConfirmedRate: number | null;
  autoConfirmedAcceptanceRate: number | null;
  duplicateReviewQueueSize: number;
  averagePotentialReviewTimeMinutes: number | null;
  potentialConfirmationRate: number | null;
  duplicateWorkSavedCount: number;
  resolvedThroughPrimaryCount: number;
};

export type KbSuggestionMetricsResponseBean = {
  suggestionAttempts: number;

  averageSuggestionConfidence?: number | null;
  averageSuggestionSimilarity?: number | null;
  autoSuggestionAcceptanceRate?: number | null;
  autoSuggestionRejectionRate?: number | null;

  manualSuggestionCount: number;
  manualSuggestionAcceptanceRate?: number | null;
  manualSuggestionRejectionRate?: number | null;
};

export type KbDraftMetricsResponseBean = {
  draftGenerationAttempts: number;
  draftGenerationSuccessRate: number;
  averageDraftGenerationConfidence?: number | null;

  submittedForReviewCount: number;
  draftApprovalRate: number;
  draftRejectionRate: number;
  averageReviewTurnaroundHours?: number | null;

  publishedAiDraftCount: number;
};

export type AiSummaryMetricsResponseBean = {
  triage: TriageMetricsResponseBean;
  routing: RoutingMetricsResponseBean;
  duplicate: DuplicateMetricsResponseBean;
  kbSuggestion: KbSuggestionMetricsResponseBean;
  kbDraft: KbDraftMetricsResponseBean;
};