import { useQuery } from "@tanstack/react-query";
import {
  fetchCategoryTrends,
  fetchDailyTrends,
  fetchDistrictRiskRanking,
  fetchHotspotRankings,
} from "@services/analytics";

export const useDailyTrends = () =>
  useQuery(["analytics", "daily"], fetchDailyTrends, {
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useCategoryTrends = () =>
  useQuery(["analytics", "categories"], fetchCategoryTrends, {
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useHotspotRankings = (limit = 5) =>
  useQuery(
    ["analytics", "hotspots", limit],
    () => fetchHotspotRankings(limit),
    {
      staleTime: 1000 * 60 * 2,
      retry: 2,
    },
  );

export const useDistrictRiskRanking = () =>
  useQuery(["analytics", "risk-ranking"], fetchDistrictRiskRanking, {
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });
