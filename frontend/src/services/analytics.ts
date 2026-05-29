import { apiClient, unwrapApiResponse } from "@services/api";
import {
  ApiResponse,
  CategoryTrendResponse,
  DashboardInsightResponse,
  DailyTrendResponse,
  DistrictRiskRankingResponse,
  HotspotResponse,
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
