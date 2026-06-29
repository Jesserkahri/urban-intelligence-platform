import { AxiosResponse } from "axios";
import { apiClient, unwrapApiResponse } from "@services/api";
import {
  ApiResponse,
  Incident,
  IncidentCreateRequest,
  IncidentUpdateRequest,
  IncidentStatus,
  PageableResponse,
  User,
} from "@appTypes/api";

export interface IncidentQueryParams {
  page?: number;
  size?: number;
  sort?: string;
  status?: IncidentStatus;
  districtId?: number;
  search?: string;
}

function applyFilters(
  pageData: PageableResponse<Incident>,
  params: IncidentQueryParams,
): PageableResponse<Incident> {
  const searchTerm = params.search?.trim().toLowerCase();
  let content = pageData.content;

  if (params.status) {
    content = content.filter((incident) => incident.status === params.status);
  }

  if (params.districtId !== undefined) {
    content = content.filter(
      (incident) => incident.districtId === params.districtId,
    );
  }

  if (searchTerm) {
    content = content.filter((incident) => {
      return [
        incident.type,
        incident.description,
        incident.status,
        incident.severity,
        incident.districtName,
      ]
        .filter(Boolean)
        .some((value) => value?.toString().toLowerCase().includes(searchTerm));
    });
  }

  const size = params.size ?? pageData.size;
  const totalElements = content.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));

  return {
    ...pageData,
    content: content.slice(0, size),
    totalElements,
    totalPages,
    number: 0,
    first: totalPages === 0 || 0 === 0,
    last: totalPages <= 1,
  };
}

export async function fetchIncidents(
  params: IncidentQueryParams = {},
): Promise<PageableResponse<Incident>> {
  const queryParams = {
    page: params.page ?? 0,
    size: params.search
      ? Math.max(params.size ?? 20, 100)
      : (params.size ?? 20),
    sort: params.sort ?? "createdAt,desc",
  };

  let response: AxiosResponse<ApiResponse<PageableResponse<Incident>>>;

  if (params.status && !params.districtId && !params.search) {
    response = await apiClient.get(`/api/incidents/status/${params.status}`, {
      params: queryParams,
    });
  } else if (
    params.districtId !== undefined &&
    !params.status &&
    !params.search
  ) {
    response = await apiClient.get(
      `/api/incidents/district/${params.districtId}`,
      { params: queryParams },
    );
  } else {
    response = await apiClient.get("/api/incidents", { params: queryParams });
  }

  const pageData = unwrapApiResponse(response);
  if (params.search || params.status || params.districtId) {
    return applyFilters(pageData, params);
  }

  return pageData;
}

export async function fetchUsers(): Promise<User[]> {
  const response = await apiClient.get<ApiResponse<User[]>>(
    "/api/users",
  );
  return response.data ?? [];
}



export async function getIncidentById(id: number): Promise<Incident> {
  const response = await apiClient.get<ApiResponse<Incident>>(
    `/api/incidents/${id}`,
  );
  return unwrapApiResponse(response);
}

export async function createIncident(
  data: IncidentCreateRequest,
): Promise<Incident> {
  const response = await apiClient.post<ApiResponse<Incident>>(
    "/api/incidents",
    data,
  );
  return unwrapApiResponse(response);
}

export async function updateIncident(
  id: number,
  data: IncidentUpdateRequest,
): Promise<Incident> {
  const response = await apiClient.put<ApiResponse<Incident>>(
    `/api/incidents/${id}`,
    data,
  );
  return unwrapApiResponse(response);
}

export async function deleteIncident(id: number): Promise<void> {
  await apiClient.delete<ApiResponse<void>>(`/api/incidents/${id}`);
}
