import { API_BASE_URL, apiClient, unwrapApiResponse } from "@services/api";
import {
  ApiResponse,
  LiveDashboardSnapshot,
  OperationalNotification,
  PageableResponse,
} from "@appTypes/api";

export interface NotificationQueryParams {
  page?: number;
  size?: number;
  unreadOnly?: boolean;
  severity?: string;
}

export function buildOperationsStreamUrl(channel = "all"): string | null {
  const tokens = apiClient.getTokens();
  if (!tokens?.accessToken) return null;
  const url = new URL(`${API_BASE_URL}/api/operations/stream`);
  url.searchParams.set("channel", channel);
  url.searchParams.set("access_token", tokens.accessToken);
  return url.toString();
}

export async function fetchLiveDashboardSnapshot(): Promise<LiveDashboardSnapshot> {
  const response = await apiClient.get<ApiResponse<LiveDashboardSnapshot>>(
    "/api/operations/dashboard/live",
  );
  return unwrapApiResponse(response);
}

export async function fetchNotifications(
  params: NotificationQueryParams = {},
): Promise<PageableResponse<OperationalNotification>> {
  const response = await apiClient.get<
    ApiResponse<PageableResponse<OperationalNotification>>
  >("/api/operations/notifications", {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      unreadOnly: params.unreadOnly,
      severity: params.severity,
    },
  });
  return unwrapApiResponse(response);
}

export async function acknowledgeNotification(
  id: number,
): Promise<OperationalNotification> {
  const response = await apiClient.post<ApiResponse<OperationalNotification>>(
    `/api/operations/notifications/${id}/acknowledge`,
  );
  return unwrapApiResponse(response);
}
