import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createIncident,
  deleteIncident,
  fetchIncidents,
  fetchRecentIncidents,
  updateIncident,
} from "@services/incident";
import {
  Incident,
  IncidentCreateRequest,
  IncidentQueryParams,
  IncidentUpdateRequest,
} from "@appTypes/api";

export const useIncidents = (params: IncidentQueryParams) => {
  return useQuery(
    [
      "incidents",
      params.page ?? 0,
      params.size ?? 20,
      params.sort ?? "createdAt,desc",
      params.status ?? null,
      params.districtId ?? null,
      params.search ?? null,
    ],
    () => fetchIncidents(params),
    {
      keepPreviousData: true,
      retry: 2,
      staleTime: 1000 * 60 * 3,
    },
  );
};

export const useRecentIncidents = () =>
  useQuery(["incidents", "recent"], fetchRecentIncidents, {
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });

export const useCreateIncident = () => {
  const queryClient = useQueryClient();

  return useMutation(
    (payload: IncidentCreateRequest) => createIncident(payload),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(["incidents"]);
        queryClient.invalidateQueries(["incidents", "recent"]);
      },
    },
  );
};

export const useUpdateIncident = () => {
  const queryClient = useQueryClient();

  return useMutation(
    ({ id, payload }: { id: number; payload: IncidentUpdateRequest }) =>
      updateIncident(id, payload),
    {
      onMutate: async ({ id, payload }) => {
        await queryClient.cancelQueries(["incidents"]);

        const previous = queryClient.getQueriesData(["incidents"]);

        queryClient.setQueriesData(["incidents"], (old: any) => {
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
        queryClient.invalidateQueries(["incidents"]);
        queryClient.invalidateQueries(["incidents", "recent"]);
      },
    },
  );
};

export const useDeleteIncident = () => {
  const queryClient = useQueryClient();

  return useMutation((id: number) => deleteIncident(id), {
    onMutate: async (id: number) => {
      await queryClient.cancelQueries(["incidents"]);

      const previous = queryClient.getQueriesData(["incidents"]);

      queryClient.setQueriesData(["incidents"], (old: any) => {
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
      queryClient.invalidateQueries(["incidents"]);
      queryClient.invalidateQueries(["incidents", "recent"]);
    },
  });
};
