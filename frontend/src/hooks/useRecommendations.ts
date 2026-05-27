import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createRecommendation,
  deleteRecommendation,
  fetchRecommendations,
  updateRecommendation,
  RecommendationQueryParams,
} from "@services/recommendations";
import {
  Recommendation,
  RecommendationCreateRequest,
  RecommendationUpdateRequest,
} from "@appTypes/api";

export const useRecommendations = (params: RecommendationQueryParams = {}) =>
  useQuery(
    [
      "recommendations",
      params.page ?? 0,
      params.size ?? 20,
      params.sort ?? "priority,desc",
    ],
    () => fetchRecommendations(params),
    {
      keepPreviousData: true,
      staleTime: 1000 * 60 * 3,
      retry: 2,
    },
  );

export const useCreateRecommendation = () => {
  const queryClient = useQueryClient();
  return useMutation(
    (payload: RecommendationCreateRequest) => createRecommendation(payload),
    {
      onSuccess: () => queryClient.invalidateQueries(["recommendations"]),
    },
  );
};

export const useUpdateRecommendation = () => {
  const queryClient = useQueryClient();
  return useMutation(
    ({ id, payload }: { id: number; payload: RecommendationUpdateRequest }) =>
      updateRecommendation(id, payload),
    {
      onSuccess: () => queryClient.invalidateQueries(["recommendations"]),
    },
  );
};

export const useDeleteRecommendation = () => {
  const queryClient = useQueryClient();
  return useMutation((id: number) => deleteRecommendation(id), {
    onSuccess: () => queryClient.invalidateQueries(["recommendations"]),
  });
};
