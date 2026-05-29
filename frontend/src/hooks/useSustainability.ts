import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  fetchDashboard,
  fetchDistrictDashboard,
  fetchRanking,
  fetchImprovingDistricts,
  fetchDecliningDistricts,
  fetchEnvironmentalAlerts,
  fetchEnvironmentalSummary,
  fetchMobilitySummary,
  fetchDistrictTrend,
  fetchDistrictMetrics,
  fetchLatestMetric,
  fetchMetricsByType,
  recordMetric,
  calculateDistrictScore,
  calculateAllScores,
} from "@services/sustainability";
import type { SustainabilityMetricCreateRequest } from "@appTypes/sustainability";

export const useSustainabilityDashboard = () =>
  useQuery({
    queryKey: ["sustainability", "dashboard"],
    queryFn: fetchDashboard,
    staleTime: 1000 * 60 * 5,
    retry: 2,
  });

export const useDistrictDashboard = (districtId: number | null) =>
  useQuery({
    queryKey: ["sustainability", "districtDashboard", districtId],
    queryFn: () => fetchDistrictDashboard(districtId!),
    enabled: districtId !== null && districtId !== undefined,
    staleTime: 1000 * 60 * 5,
    retry: 2,
  });

export const useSustainabilityRanking = () =>
  useQuery({
    queryKey: ["sustainability", "ranking"],
    queryFn: fetchRanking,
    staleTime: 1000 * 60 * 5,
    retry: 2,
  });

export const useImprovingDistricts = () =>
  useQuery({
    queryKey: ["sustainability", "improving"],
    queryFn: fetchImprovingDistricts,
    staleTime: 1000 * 60 * 5,
    retry: 2,
  });

export const useDecliningDistricts = () =>
  useQuery({
    queryKey: ["sustainability", "declining"],
    queryFn: fetchDecliningDistricts,
    staleTime: 1000 * 60 * 5,
    retry: 2,
  });

export const useEnvironmentalAlerts = () =>
  useQuery({
    queryKey: ["sustainability", "alerts"],
    queryFn: fetchEnvironmentalAlerts,
    staleTime: 1000 * 30,
    retry: 2,
  });

export const useEnvironmentalSummary = (days = 30) =>
  useQuery({
    queryKey: ["sustainability", "environmentalSummary", days],
    queryFn: () => fetchEnvironmentalSummary(days),
    staleTime: 1000 * 60 * 10,
    retry: 2,
  });

export const useMobilitySummary = (days = 30) =>
  useQuery({
    queryKey: ["sustainability", "mobilitySummary", days],
    queryFn: () => fetchMobilitySummary(days),
    staleTime: 1000 * 60 * 10,
    retry: 2,
  });

export const useDistrictTrend = (districtId: number | null, days = 90) =>
  useQuery({
    queryKey: ["sustainability", "trend", districtId, days],
    queryFn: () => fetchDistrictTrend(districtId!, days),
    enabled: districtId !== null && districtId !== undefined,
    staleTime: 1000 * 60 * 10,
    retry: 2,
  });

export const useDistrictMetrics = (districtId: number | null) =>
  useQuery({
    queryKey: ["sustainability", "districtMetrics", districtId],
    queryFn: () => fetchDistrictMetrics(districtId!),
    enabled: districtId !== null && districtId !== undefined,
    staleTime: 1000 * 60 * 5,
    retry: 2,
  });

export const useLatestMetric = (
  districtId: number | null,
  metricType: string | null,
) =>
  useQuery({
    queryKey: ["sustainability", "latestMetric", districtId, metricType],
    queryFn: () => fetchLatestMetric(districtId!, metricType!),
    enabled:
      districtId !== null &&
      districtId !== undefined &&
      metricType !== null &&
      metricType !== undefined,
    staleTime: 1000 * 30,
    retry: 2,
  });

export const useMetricsByType = (metricType: string | null) =>
  useQuery({
    queryKey: ["sustainability", "metricsByType", metricType],
    queryFn: () => fetchMetricsByType(metricType!),
    enabled: metricType !== null && metricType !== undefined,
    staleTime: 1000 * 60 * 5,
    retry: 2,
  });

export const useRecordMetric = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: SustainabilityMetricCreateRequest) => recordMetric(data),
    onSuccess: () =>
      queryClient.invalidateQueries({
        queryKey: ["sustainability"],
      }),
  });
};

export const useCalculateDistrictScore = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (districtId: number) => calculateDistrictScore(districtId),
    onSuccess: () =>
      queryClient.invalidateQueries({
        queryKey: ["sustainability"],
      }),
  });
};

export const useCalculateAllScores = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: calculateAllScores,
    onSuccess: () =>
      queryClient.invalidateQueries({
        queryKey: ["sustainability"],
      }),
  });
};
