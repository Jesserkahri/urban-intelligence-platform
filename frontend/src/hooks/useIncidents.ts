import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createIncident,
  deleteIncident,
  fetchIncidents,
  fetchRecentIncidents,
  IncidentQueryParams,
  updateIncident,
} from "@services/incident";
import {
  Incident,
  IncidentCreateRequest,
  IncidentUpdateRequest,
  PageableResponse,
} from "@appTypes/api";

export const useIncidents = (params: IncidentQueryParams) => {
  return useQuery<PageableResponse<Incident>, Error>({
    queryKey: [
      "incidents",
      params.page ?? 0,
      params.size ?? 20,
      params.sort ?? "createdAt,desc",
      params.status ?? null,
      params.districtId ?? null,
      params.search ?? null,
    ],
    queryFn: () => fetchIncidents(params),
    placeholderData: (prev) => prev,
    retry: 2,
    staleTime: 1000 * 60 * 3,
  });
};

export const useRecentIncidents = () =>
  useQuery<Incident[], Error>({
    queryKey: ["incidents", "recent"],
    queryFn: fetchRecentIncidents,
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useCreateIncident = () => {
  const queryClient = useQueryClient();

  return useMutation<Incident, Error, IncidentCreateRequest>({
    mutationFn: (payload: IncidentCreateRequest) => createIncident(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["incidents"] });
      queryClient.invalidateQueries({ queryKey: ["incidents", "recent"] });
      queryClient.invalidateQueries({ queryKey: ["analytics"] });
    },
  });
};

export const useUpdateIncident = () => {
  const queryClient = useQueryClient();

  return useMutation<
    Incident,
    Error,
    { id: number; payload: IncidentUpdateRequest }
  >({
    mutationFn: ({ id, payload }) => updateIncident(id, payload),
    onMutate: async ({ id, payload }) => {
      await queryClient.cancelQueries({ queryKey: ["incidents"] });

      const previous = queryClient.getQueriesData({ queryKey: ["incidents"] });

      queryClient.setQueriesData({ queryKey: ["incidents"] }, (old: any) => {
        if (!old || !old.content) return old;
        return {
          ...old,
          content: old.content.map((incident: Incident) =>
            incident.id === id ? { ...incident, ...payload } : incident,
          ),
        };
      });

      return { previous };
    },
    onError: (_, __, context: any) => {
      if (context?.previous) {
        context.previous.forEach(([queryKey, data]: any) => {
          queryClient.setQueryData(queryKey, data);
        });
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["incidents"] });
      queryClient.invalidateQueries({ queryKey: ["incidents", "recent"] });
      queryClient.invalidateQueries({ queryKey: ["analytics"] });
    },
  });
};

export const useDeleteIncident = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, number>({
    mutationFn: (id: number) => deleteIncident(id),
    onMutate: async (id: number) => {
      await queryClient.cancelQueries({ queryKey: ["incidents"] });

      const previous = queryClient.getQueriesData({ queryKey: ["incidents"] });

      queryClient.setQueriesData({ queryKey: ["incidents"] }, (old: any) => {
        if (!old || !old.content) return old;
        return {
          ...old,
          content: old.content.filter(
            (incident: Incident) => incident.id !== id,
          ),
          totalElements: Math.max(0, (old.totalElements ?? 0) - 1),
        };
      });

      return { previous };
    },
    onError: (_, __, context: any) => {
      if (context?.previous) {
        context.previous.forEach(([queryKey, data]: any) => {
          queryClient.setQueryData(queryKey, data);
        });
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["incidents"] });
      queryClient.invalidateQueries({ queryKey: ["incidents", "recent"] });
    },
  });
};
