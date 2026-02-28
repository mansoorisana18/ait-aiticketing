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

export type LoginResponseBean = {
  userId: number;
  email: string;
  name: string;
  role: UserRole;
  sessionToken: string; // currently returned by backend
};

export type TicketResponseBean = {
  ticketId: number;
  title: string;
  description: string;
  status: string; // OPEN, etc
  createdAt: string;
  updatedAt: string;
  createdByUserId: number;
  createdByName: string;
  createdByEmail: string;

  assignedToUserId?: number;
  assignedToName?: string;
  aiCategory?: string;
  aiPriority?: string;
};

export type UserResponseBean = {
  userId: number;
  email: string;
  name: string;
  role: UserRole;
};