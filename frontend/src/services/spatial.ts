import { ApiResponse, Incident, IncidentSeverity } from "@appTypes/api";
import { apiClient, unwrapApiResponse } from "@services/api";

export interface IncidentGeoQueryParams {
  minLat?: number;
  maxLat?: number;
  minLon?: number;
  maxLon?: number;
  severity?: IncidentSeverity;
  districtId?: number;
}

export async function fetchIncidentGeoPoints(
  params: IncidentGeoQueryParams = {},
): Promise<Incident[]> {
  const response = await apiClient.get<ApiResponse<Incident[]>>(
    "/api/incidents/geo",
    {
      params,
    },
  );
  return unwrapApiResponse(response);
}
