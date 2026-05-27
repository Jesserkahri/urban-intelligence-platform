import { apiClient } from "@services/api";
import {
  CategoryTrendResponse,
  DashboardInsightResponse,
  DailyTrendResponse,
  DistrictRiskRankingResponse,
  HotspotResponse,
  WeeklyTrendResponse,
} from "@appTypes/api";

export async function fetchDailyTrends(): Promise<DailyTrendResponse> {
  const response = await apiClient.get<DailyTrendResponse>(
    "/api/analytics/trends/daily",
  );
  return response.data;
}

export async function fetchCategoryTrends(): Promise<CategoryTrendResponse> {
  const response = await apiClient.get<CategoryTrendResponse>(
    "/api/analytics/trends/categories",
  );
  return response.data;
}

export async function fetchHotspotRankings(
  limit = 10,
): Promise<HotspotResponse[]> {
  const response = await apiClient.get<HotspotResponse[]>(
    "/api/analytics/hotspots/top",
    { params: { limit } },
  );
  return response.data;
}

export async function fetchHotspots(): Promise<HotspotResponse[]> {
  const response = await apiClient.get<HotspotResponse[]>(
    "/api/analytics/hotspots",
  );
  return response.data;
}

export async function fetchDistrictRiskRanking(): Promise<
  DistrictRiskRankingResponse[]
> {
  const response = await apiClient.get<DistrictRiskRankingResponse[]>(
    "/api/analytics/districts/risk-ranking",
  );
  return response.data;
}

export async function fetchDashboardInsights(): Promise<DashboardInsightResponse> {
  const response = await apiClient.get<DashboardInsightResponse>(
    "/api/analytics/insights/dashboard",
  );
  return response.data;
}

export async function fetchWeeklyTrends(): Promise<WeeklyTrendResponse> {
  const response = await apiClient.get<WeeklyTrendResponse>(
    "/api/analytics/trends/weekly",
  );
  return response.data;
}
