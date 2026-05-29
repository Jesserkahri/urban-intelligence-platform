// API Response types
export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  message?: string;
  timestamp?: string;
}

export interface PageableResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
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

export type Role = "ADMIN" | "OPERATOR" | "ANALYST" | "VIEWER";

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
  incidentCount?: number;
  recommendationCount?: number;
}

export interface DistrictResponse {
  id: number;
  name: string;
  population: number;
  sustainabilityScore: number;
  operationalRiskScore: number;
}

export interface DistrictMetricsResponse {
  id: number;
  name: string;
  healthScore: number;
  recentIncidentsCount: number;
  sustainabilityScore: number;
  operationalRiskScore: number;
}

// Incident types
export type IncidentSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type IncidentStatus =
  | "REPORTED"
  | "OPEN"
  | "IN_PROGRESS"
  | "RESOLVED"
  | "CLOSED";

export interface Incident {
  id: number;
  type: string;
  title?: string;
  description: string;
  latitude: number;
  longitude: number;
  districtId: number;
  districtName?: string;
  severity: IncidentSeverity;
  status: IncidentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface IncidentCreateRequest {
  type: string;
  description: string;
  severity: IncidentSeverity;
  latitude: number;
  longitude: number;
  districtId: number;
}

export interface IncidentUpdateRequest {
  type?: string;
  description?: string;
  severity?: IncidentSeverity;
  latitude?: number;
  longitude?: number;
  status?: IncidentStatus;
}

// Analytics types
export interface DailyTrendResponse {
  startDate: string;
  endDate: string;
  totalIncidents: number;
  averageDaily: number;
  growthPercentage: number;
  trendIndicator: string;
  dailyData: Array<{
    date: string;
    incidentCount: number;
    criticalCount: number;
    resolvedCount: number;
  }>;
}

export interface WeeklyTrendResponse {
  weeksAnalyzed: number;
  totalIncidents: number;
  averageWeekly: number;
  weeklyData: Array<{
    week: number;
    incidentCount: number;
    categoryBreakdown: Record<string, number>;
    severityDistribution: Record<string, number>;
    resolutionRate: number;
  }>;
}

export interface CategoryTrendResponse {
  analysisWindow: number;
  totalIncidents: number;
  uniqueCategories: number;
  topCategory: string;
  categoryData: Array<{
    category: string;
    count: number;
    percentage: number;
    averageSeverity: number;
    resolutionRate: number;
  }>;
}

export interface HotspotResponse {
  rank?: number;
  districtId: number;
  districtName: string;
  hotspotScore: number;
  incidentCount: number;
  unresolvedIncidentCount?: number;
  unresolvedIncidents?: number;
  unresolvedRatio: number;
  averageSeverity: string;
  riskIntensity?: string;
  criticalityLevel?: string;
}

export interface DistrictRiskRankingResponse {
  districtId: number;
  districtName: string;
  riskScore: number;
  riskLevel: string;
  incidentCount: number;
  unresolvedCount: number;
  population: number;
}

export interface OperationalInsightResponse {
  districtId: number;
  districtName: string;
  generatedRecommendationCount: number;
  criticalRecommendations: number;
  highPriorityRecommendations: number;
  recommendations: Array<{
    id: number;
    type: string;
    priority: string;
    message: string;
    districtId: number;
    districtName: string;
    createdAt: string;
    updatedAt: string;
  }>;
  generatedAt: string;
}

export interface DashboardInsightResponse {
  health: {
    totalIncidents24h: number;
    criticalIncidents: number;
    highSeverityIncidents: number;
    systemStatus: string;
  };
  alerts: Array<{
    type: string;
    message: string;
    priority: string;
    count: number;
    category: string;
  }>;
  recommendations: Array<{
    title: string;
    rationale: string;
    priority: string;
  }>;
  trendSummary: {
    trendDirection: string;
    growthPercentage: number;
    summary: string;
    topIncidentTypes: string[];
    topCategories: string[];
  };
  intelligenceCards: Array<{
    title: string;
    detail: string;
    severity: string;
  }>;
  generatedAt: string;
}

// Recommendation types
export type RecommendationPriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type RecommendationStatus =
  | "PENDING"
  | "IMPLEMENTED"
  | "REJECTED"
  | "APPROVED"
  | "DECLINED";

export interface Recommendation {
  id: number;
  type: string;
  priority: RecommendationPriority;
  message: string;
  status?: RecommendationStatus;
  predictedImpact?: number;
  interventionEffectiveness?: number;
  operationalConfidence?: number;
  districtId: number;
  districtName?: string;
  createdAt: string;
  updatedAt?: string;
}

// Workflow & Operational types
export interface ActivityEvent {
  id: number;
  entityType: string;
  entityId: number;
  action: string;
  performer: string;
  details: string;
  timestamp: string;
}

export interface IncidentAssignmentRequest {
  assignedTo: string;
  notes?: string;
}

export interface IncidentReviewRequest {
  reviewStatus: "ACKNOWLEDGED" | "REVIEWED" | "REJECTED";
  reviewNotes: string;
}

export interface RecommendationDecisionRequest {
  decision: "APPROVED" | "REJECTED";
  reason: string;
  implementationNotes?: string;
}

export interface RecommendationCreateRequest {
  type: string;
  priority: RecommendationPriority;
  message: string;
  districtId: number;
}

export interface RecommendationUpdateRequest {
  type?: string;
  priority?: RecommendationPriority;
  message?: string;
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
