import { useQuery } from "@tanstack/react-query";
import {
  fetchDistricts,
  fetchDistrictsByHighestRisk,
} from "@services/district";
import { DistrictQueryParams } from "@services/district";

export const useDistricts = (params: DistrictQueryParams = {}) =>
  useQuery({
    queryKey: [
      "districts",
      params.page ?? 0,
      params.size ?? 20,
      params.sort ?? "name,asc",
    ],
    queryFn: () => fetchDistricts(params),
    placeholderData: (prev) => prev,
    staleTime: 1000 * 60 * 3,
    retry: 2,
  });

export const useHighestRiskDistricts = () =>
  useQuery({
    queryKey: ["districts", "highest-risk"],
    queryFn: fetchDistrictsByHighestRisk,
    staleTime: 1000 * 60 * 5,
    retry: 2,
  });
