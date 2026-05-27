import { apiClient, unwrapApiResponse } from "@services/api";
import {
  ApiResponse,
  PageableResponse,
  Recommendation,
  RecommendationCreateRequest,
  RecommendationUpdateRequest,
} from "@appTypes/api";

export interface RecommendationQueryParams {
  page?: number;
  size?: number;
  sort?: string;
}

export async function fetchRecommendations(
  params: RecommendationQueryParams = {},
): Promise<PageableResponse<Recommendation>> {
  const response = await apiClient.get<
    ApiResponse<PageableResponse<Recommendation>>
  >("/api/recommendations", {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? "priority,desc",
    },
  });
  return unwrapApiResponse(response);
}

export async function createRecommendation(
  data: RecommendationCreateRequest,
): Promise<Recommendation> {
  const response = await apiClient.post<ApiResponse<Recommendation>>(
    "/api/recommendations",
    data,
  );
  return unwrapApiResponse(response);
}

export async function updateRecommendation(
  id: number,
  data: RecommendationUpdateRequest,
): Promise<Recommendation> {
  const response = await apiClient.put<ApiResponse<Recommendation>>(
    `/api/recommendations/${id}`,
    data,
  );
  return unwrapApiResponse(response);
}

export async function deleteRecommendation(id: number): Promise<void> {
  await apiClient.delete<ApiResponse<void>>(`/api/recommendations/${id}`);
}
