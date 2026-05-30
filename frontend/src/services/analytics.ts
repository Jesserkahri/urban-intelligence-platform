import { apiClient, unwrapApiResponse } from "@services/api";
import {
  ApiResponse,
  AnomalyResponse,
  CategoryTrendResponse,
  DashboardInsightResponse,
  DailyTrendResponse,
  DistrictRiskRankingResponse,
  ForecastResponse,
  HotspotResponse,
  RecommendationExplanationResponse,
  RiskExplanationResponse,
  SpatialRiskResponse,
  WeeklyTrendResponse,
} from "@appTypes/api";

export async function fetchDailyTrends(): Promise<DailyTrendResponse> {
  const response = await apiClient.get<ApiResponse<DailyTrendResponse>>(
    "/api/analytics/trends/daily",
  );
  return unwrapApiResponse(response);
}

export async function fetchCategoryTrends(): Promise<CategoryTrendResponse> {
  const response = await apiClient.get<ApiResponse<CategoryTrendResponse>>(
    "/api/analytics/trends/categories",
  );
  return unwrapApiResponse(response);
}

export async function fetchHotspotRankings(
  limit = 10,
): Promise<HotspotResponse[]> {
  const response = await apiClient.get<ApiResponse<HotspotResponse[]>>(
    "/api/analytics/hotspots/top",
    { params: { limit } },
  );
  return unwrapApiResponse(response);
}

export async function fetchHotspots(): Promise<HotspotResponse[]> {
  const response = await apiClient.get<ApiResponse<HotspotResponse[]>>(
    "/api/analytics/hotspots",
  );
  return unwrapApiResponse(response);
}

export async function fetchDistrictRiskRanking(): Promise<
  DistrictRiskRankingResponse[]
> {
  const response = await apiClient.get<
    ApiResponse<DistrictRiskRankingResponse[]>
  >(
    "/api/analytics/districts/risk-ranking",
  );
  return unwrapApiResponse(response);
}

export async function fetchDashboardInsights(): Promise<DashboardInsightResponse> {
  const response = await apiClient.get<ApiResponse<DashboardInsightResponse>>(
    "/api/analytics/insights/dashboard",
  );
  return unwrapApiResponse(response);
}

export async function fetchWeeklyTrends(): Promise<WeeklyTrendResponse> {
  const response = await apiClient.get<ApiResponse<WeeklyTrendResponse>>(
    "/api/analytics/trends/weekly",
  );
  return unwrapApiResponse(response);
}

export async function fetchAnomalies(): Promise<AnomalyResponse[]> {
  const response = await apiClient.get<ApiResponse<AnomalyResponse[]>>(
    "/api/analytics/intelligence/anomalies",
  );
  return unwrapApiResponse(response);
}

export async function fetchForecasts(days = 7): Promise<ForecastResponse[]> {
  const response = await apiClient.get<ApiResponse<ForecastResponse[]>>(
    "/api/analytics/intelligence/forecasts",
    { params: { days } },
  );
  return unwrapApiResponse(response);
}

export async function fetchSpatialRisk(): Promise<SpatialRiskResponse[]> {
  const response = await apiClient.get<ApiResponse<SpatialRiskResponse[]>>(
    "/api/analytics/intelligence/spatial-risk",
  );
  return unwrapApiResponse(response);
}

export async function fetchRiskExplanations(): Promise<RiskExplanationResponse[]> {
  const response = await apiClient.get<ApiResponse<RiskExplanationResponse[]>>(
    "/api/analytics/intelligence/risk-explanations",
  );
  return unwrapApiResponse(response);
}

export async function fetchRecommendationExplanations(): Promise<
  RecommendationExplanationResponse[]
> {
  const response = await apiClient.get<
    ApiResponse<RecommendationExplanationResponse[]>
  >("/api/analytics/intelligence/recommendation-explanations");
  return unwrapApiResponse(response);
}
