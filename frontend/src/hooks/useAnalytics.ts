import { useQuery } from "@tanstack/react-query";
import {
  fetchCategoryTrends,
  fetchDailyTrends,
  fetchDashboardInsights,
  fetchDistrictRiskRanking,
  fetchHotspotRankings,
} from "@services/analytics";
import {
  CategoryTrendResponse,
  DashboardInsightResponse,
  DailyTrendResponse,
  DistrictRiskRankingResponse,
  HotspotResponse,
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
