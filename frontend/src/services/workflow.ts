import { apiClient, unwrapApiResponse } from "@services/api";
import {
  ApiResponse,
  ActivityEvent,
  IncidentAssignmentRequest,
  IncidentReviewRequest,
  RecommendationDecisionRequest,
} from "@appTypes/api";

// Incident Workflow Operations
export async function assignIncident(
  incidentId: number,
  request: IncidentAssignmentRequest,
): Promise<void> {
  await apiClient.post<ApiResponse<void>>(
    `/api/incidents/${incidentId}/assign`,
    request,
  );
}

export async function acknowledgeIncident(
  incidentId: number,
  notes?: string,
): Promise<void> {
  await apiClient.post<ApiResponse<void>>(
    `/api/incidents/${incidentId}/acknowledge`,
    { notes },
  );
}

export async function reviewIncident(
  incidentId: number,
  request: IncidentReviewRequest,
): Promise<void> {
  await apiClient.post<ApiResponse<void>>(
    `/api/incidents/${incidentId}/review`,
    request,
  );
}

// Recommendation Workflow Operations
export async function approveRecommendation(
  recommendationId: number,
  request: RecommendationDecisionRequest,
): Promise<void> {
  await apiClient.post<ApiResponse<void>>(
    `/api/recommendations/${recommendationId}/approve`,
    request,
  );
}

export async function rejectRecommendation(
  recommendationId: number,
  request: RecommendationDecisionRequest,
): Promise<void> {
  await apiClient.post<ApiResponse<void>>(
    `/api/recommendations/${recommendationId}/reject`,
    request,
  );
}

// Activity Timeline
export async function getActivityTimeline(
  entityType: string,
  entityId: number,
): Promise<ActivityEvent[]> {
  const response = await apiClient.get<ApiResponse<ActivityEvent[]>>(
    `/api/activities/${entityType}/${entityId}/timeline`,
  );
  return unwrapApiResponse(response);
}

export async function getAllActivities(): Promise<ActivityEvent[]> {
  const response =
    await apiClient.get<ApiResponse<ActivityEvent[]>>("/api/activities");
  return unwrapApiResponse(response);
}
