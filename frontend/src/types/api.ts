// API Response types
export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  message?: string;
  timestamp?: string;
}

// Auth types
export interface LoginRequest {
  login: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  displayName: string;
  role: Role;
  emailVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export type Role = "ADMIN" | "MANAGER" | "VIEWER";

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

// District types
export interface District {
  id: number;
  name: string;
  population: number;
  sustainabilityScore: number;
  operationalRiskScore: number;
  createdAt: string;
  updatedAt: string;
}

export interface DistrictResponse {
  id: number;
  name: string;
  population: number;
  sustainabilityScore: number;
  operationalRiskScore: number;
}

// Incident types
export interface Incident {
  id: number;
  districtId: number;
  title: string;
  description: string;
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  status: "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";
  createdAt: string;
  updatedAt: string;
}

// Analytics types
export interface AnalyticsData {
  date: string;
  incidents: number;
  riskScore: number;
  sustainabilityScore: number;
}

// Recommendation types
export interface Recommendation {
  id: number;
  districtId: number;
  title: string;
  description: string;
  priority: "LOW" | "MEDIUM" | "HIGH";
  status: "PENDING" | "IMPLEMENTED" | "REJECTED";
  createdAt: string;
}

// KPI types
export interface KPI {
  label: string;
  value: number | string;
  change?: number;
  trend?: "up" | "down" | "neutral";
  unit?: string;
}

// Error types
export interface ApiError {
  message: string;
  code?: string;
  status?: number;
  timestamp?: string;
}
