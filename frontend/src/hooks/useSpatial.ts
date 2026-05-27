import { useQuery } from "@tanstack/react-query";
import {
  fetchIncidentGeoPoints,
  IncidentGeoQueryParams,
} from "@services/spatial";
import { Incident } from "@appTypes/api";

export const useIncidentGeoPoints = (params: IncidentGeoQueryParams) =>
  useQuery<Incident[], Error>({
    queryKey: ["incidents", "geo", params],
    queryFn: () => fetchIncidentGeoPoints(params),
    staleTime: 1000 * 60 * 2,
    retry: 2,
  });
