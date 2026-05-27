import { apiClient, unwrapApiResponse } from "@services/api";
import {
  ApiResponse,
  District,
  DistrictMetricsResponse,
  PageableResponse,
} from "@appTypes/api";

export interface DistrictQueryParams {
  page?: number;
  size?: number;
  sort?: string;
}

export async function fetchDistricts(
  params: DistrictQueryParams = {},
): Promise<PageableResponse<District>> {
  const response = await apiClient.get<ApiResponse<PageableResponse<District>>>(
    "/api/districts",
    {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 20,
        sort: params.sort ?? "name,asc",
      },
    },
  );
  return unwrapApiResponse(response);
}

export async function fetchDistrictMetrics(
  districtId: number,
): Promise<DistrictMetricsResponse> {
  const response = await apiClient.get<ApiResponse<DistrictMetricsResponse>>(
    `/api/districts/${districtId}/metrics`,
  );
  return unwrapApiResponse(response);
}

export async function fetchDistrictsByHighestRisk(): Promise<District[]> {
  const response = await apiClient.get<ApiResponse<District[]>>(
    "/api/districts/risk-analysis/highest",
  );
  return unwrapApiResponse(response);
}
