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
  | "READY"
  | "IN_PROGRESS"
  | "DUPLICATE"
  | "RESOLVED"
  | "CLOSED";

export type DuplicateState = "NONE" | "POTENTIAL" | "CONFIRMED";

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
  duplicateState?: DuplicateState | string | null;

  //AI: TRAIGE & ROUTING detail fields
  aiFailed?: boolean | null; //only for TRIAGE
  aiLastError?: string | null;
  aiTriagedAt?: string | null;
  vagueCount?: number | null; //no of times ticket has been marked as vague, used for both agent/admin and user view
  lastVagueAt?: string | null;
  firstAssignedAt?: string | null; //metrics field for dashboard
  vagueReason?: string | null;
  clarificationPrompt?: string | null;

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
};

export type RoutingMetricsResponseBean = {
  routingAttempts: number;
  autoRoutingSuccessRate: number | null;
  noEligibleAgentRate: number | null;
  averageTimeToAssignmentFromTriageSeconds: number | null;
  assignmentOverrideRate: number | null;
};

export type AiSummaryMetricsResponseBean = {
  triage: TriageMetricsResponseBean;
  routing: RoutingMetricsResponseBean;
};