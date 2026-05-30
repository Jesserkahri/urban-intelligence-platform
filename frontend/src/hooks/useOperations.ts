import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  acknowledgeNotification,
  fetchLiveDashboardSnapshot,
  fetchNotifications,
  NotificationQueryParams,
} from "@services/operations";

export const useLiveDashboardSnapshot = () =>
  useQuery({
    queryKey: ["operations", "dashboard"],
    queryFn: fetchLiveDashboardSnapshot,
    staleTime: 1000 * 30,
    retry: 2,
  });

export const useNotifications = (params: NotificationQueryParams = {}) =>
  useQuery({
    queryKey: [
      "operations",
      "notifications",
      params.page ?? 0,
      params.size ?? 20,
      params.unreadOnly ?? false,
      params.severity ?? null,
    ],
    queryFn: () => fetchNotifications(params),
    staleTime: 1000 * 30,
    retry: 2,
  });

export const useAcknowledgeNotification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: acknowledgeNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["operations"] });
    },
  });
};
