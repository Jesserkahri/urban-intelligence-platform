import { apiClient, unwrapApiResponse } from "@services/api";
import type {
  SustainabilityDashboardResponse,
  SustainabilityMetricResponse,
  SustainabilityMetricCreateRequest,
  SustainabilityScoreResponse,
  EnvironmentalSummaryResponse,
  MobilitySummaryResponse,
  SustainabilityTrendResponse,
} from "@appTypes/sustainability";
import type { ApiResponse } from "@appTypes/api";

export interface SustainabilityQueryParams {
  days?: number;
}

export async function fetchDashboard(): Promise<SustainabilityDashboardResponse> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityDashboardResponse>
  >("/api/sustainability/dashboard");
  return unwrapApiResponse(response);
}

export async function fetchDistrictDashboard(
  districtId: number,
): Promise<SustainabilityDashboardResponse> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityDashboardResponse>
  >(`/api/sustainability/districts/${districtId}/dashboard`);
  return unwrapApiResponse(response);
}

export async function fetchDistrictMetrics(
  districtId: number,
): Promise<SustainabilityMetricResponse[]> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityMetricResponse[]>
  >(`/api/sustainability/districts/${districtId}/metrics/recent?hoursBack=168`);
  return unwrapApiResponse(response);
}

export async function fetchRanking(): Promise<SustainabilityScoreResponse[]> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityScoreResponse[]>
  >("/api/sustainability/scores/ranking");
  return unwrapApiResponse(response);
}

export async function fetchImprovingDistricts(): Promise<
  SustainabilityScoreResponse[]
> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityScoreResponse[]>
  >("/api/sustainability/scores/improving");
  return unwrapApiResponse(response);
}

export async function fetchDecliningDistricts(): Promise<
  SustainabilityScoreResponse[]
> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityScoreResponse[]>
  >("/api/sustainability/scores/declining");
  return unwrapApiResponse(response);
}

export async function fetchEnvironmentalAlerts(): Promise<
  SustainabilityMetricResponse[]
> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityMetricResponse[]>
  >("/api/sustainability/alerts");
  return unwrapApiResponse(response);
}

export async function fetchEnvironmentalSummary(
  days = 30,
): Promise<EnvironmentalSummaryResponse> {
  const response = await apiClient.get<
    ApiResponse<EnvironmentalSummaryResponse>
  >(`/api/sustainability/environmental/summary?days=${days}`);
  return unwrapApiResponse(response);
}

export async function fetchMobilitySummary(
  days = 30,
): Promise<MobilitySummaryResponse> {
  const response = await apiClient.get<ApiResponse<MobilitySummaryResponse>>(
    `/api/sustainability/mobility/summary?days=${days}`,
  );
  return unwrapApiResponse(response);
}

export async function fetchDistrictTrend(
  districtId: number,
  days = 90,
): Promise<SustainabilityTrendResponse> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityTrendResponse>
  >(`/api/sustainability/districts/${districtId}/trends?days=${days}`);
  return unwrapApiResponse(response);
}

export async function recordMetric(
  data: SustainabilityMetricCreateRequest,
): Promise<SustainabilityMetricResponse> {
  const response = await apiClient.post<
    ApiResponse<SustainabilityMetricResponse>
  >("/api/sustainability/metrics", data);
  return unwrapApiResponse(response);
}

export async function calculateDistrictScore(
  districtId: number,
): Promise<SustainabilityScoreResponse> {
  const response = await apiClient.post<
    ApiResponse<SustainabilityScoreResponse>
  >(`/api/sustainability/scores/calculate/${districtId}`);
  return unwrapApiResponse(response);
}

export async function calculateAllScores(): Promise<void> {
  await apiClient.post("/api/sustainability/scores/calculate-all");
}

export async function fetchLatestMetric(
  districtId: number,
  metricType: string,
): Promise<SustainabilityMetricResponse> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityMetricResponse>
  >(`/api/sustainability/districts/${districtId}/metrics/${metricType}/latest`);
  return unwrapApiResponse(response);
}

export async function fetchMetricsByType(
  metricType: string,
): Promise<SustainabilityMetricResponse[]> {
  const response = await apiClient.get<
    ApiResponse<SustainabilityMetricResponse[]>
  >(`/api/sustainability/metrics/type/${metricType}`);
  return unwrapApiResponse(response);
}
