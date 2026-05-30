import { useQuery } from "@tanstack/react-query";
import {
  fetchAnomalies,
  fetchCategoryTrends,
  fetchDailyTrends,
  fetchDashboardInsights,
  fetchDistrictRiskRanking,
  fetchForecasts,
  fetchHotspotRankings,
  fetchRecommendationExplanations,
  fetchRiskExplanations,
  fetchSpatialRisk,
} from "@services/analytics";
import {
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
} from "@appTypes/api";
export const useDailyTrends = () =>
  useQuery<DailyTrendResponse, Error>({
    queryKey: ["analytics", "daily"],
    queryFn: fetchDailyTrends,
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useCategoryTrends = () =>
  useQuery<CategoryTrendResponse, Error>({
    queryKey: ["analytics", "categories"],
    queryFn: fetchCategoryTrends,
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useHotspotRankings = (limit = 5) =>
  useQuery<HotspotResponse[], Error>({
    queryKey: ["analytics", "hotspots", limit],
    queryFn: () => fetchHotspotRankings(limit),
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useDistrictRiskRanking = () =>
  useQuery<DistrictRiskRankingResponse[], Error>({
    queryKey: ["analytics", "risk-ranking"],
    queryFn: fetchDistrictRiskRanking,
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useDashboardInsights = () =>
  useQuery<DashboardInsightResponse, Error>({
    queryKey: ["analytics", "dashboard"],
    queryFn: fetchDashboardInsights,
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useAnomalies = () =>
  useQuery<AnomalyResponse[], Error>({
    queryKey: ["analytics", "intelligence", "anomalies"],
    queryFn: fetchAnomalies,
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useForecasts = (days = 7) =>
  useQuery<ForecastResponse[], Error>({
    queryKey: ["analytics", "intelligence", "forecasts", days],
    queryFn: () => fetchForecasts(days),
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useSpatialRisk = () =>
  useQuery<SpatialRiskResponse[], Error>({
    queryKey: ["analytics", "intelligence", "spatial-risk"],
    queryFn: fetchSpatialRisk,
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useRiskExplanations = () =>
  useQuery<RiskExplanationResponse[], Error>({
    queryKey: ["analytics", "intelligence", "risk-explanations"],
    queryFn: fetchRiskExplanations,
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useRecommendationExplanations = () =>
  useQuery<RecommendationExplanationResponse[], Error>({
    queryKey: ["analytics", "intelligence", "recommendation-explanations"],
    queryFn: fetchRecommendationExplanations,
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });
