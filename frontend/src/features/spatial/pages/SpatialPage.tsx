import React, { useMemo, useState } from "react";
import {
  MapContainer,
  TileLayer,
  CircleMarker,
  Circle,
  GeoJSON,
  Popup,
  useMapEvents,
} from "react-leaflet";
import { AlertTriangle, Globe2, Layers, MapPin, Target } from "lucide-react";
import { useDistricts } from "@hooks/useDistricts";
import { useIncidentGeoPoints } from "@hooks/useSpatial";
import { districtGeoJson } from "@/data/districtBoundaries";
import { Incident, District } from "@appTypes/api";
import { KPICard } from "@components/common/KPICard";
import { cn } from "@lib/utils";

const severityColors: Record<string, string> = {
  CRITICAL: "#dc2626",
  HIGH: "#f97316",
  MEDIUM: "#facc15",
  LOW: "#22c55e",
};

const severityOptions = ["ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW"] as const;

const defaultCenter: [number, number] = [40.747, -73.995];

function getClusterKey(lat: number, lon: number, resolution: number) {
  return `${Math.round(lat / resolution)}-${Math.round(lon / resolution)}`;
}

function getDistrictFillColor(riskScore?: number): string {
  if (riskScore === undefined) return "rgba(15, 23, 42, 0.16)";
  if (riskScore >= 60) return "rgba(220, 38, 38, 0.24)";
  if (riskScore >= 40) return "rgba(245, 158, 11, 0.22)";
  if (riskScore >= 20) return "rgba(34, 197, 94, 0.18)";
  return "rgba(20, 184, 166, 0.16)";
}

function useMapViewport(
  setBounds: (bounds: [number, number, number, number]) => void,
  setZoom: (zoom: number) => void,
) {
  useMapEvents({
    moveend: (event) => {
      const bounds = event.target.getBounds();
      setBounds([
        bounds.getSouth(),
        bounds.getWest(),
        bounds.getNorth(),
        bounds.getEast(),
      ]);
    },
    zoomend: (event) => {
      setZoom(event.target.getZoom());
    },
  });
  return null;
}

export const SpatialPage: React.FC = () => {
  const [severityFilter, setSeverityFilter] = useState<
    "ALL" | "CRITICAL" | "HIGH" | "MEDIUM" | "LOW"
  >("ALL");
  const [selectedDistrictId, setSelectedDistrictId] = useState<number | null>(
    null,
  );
  const [showHotspots, setShowHotspots] = useState(true);
  const [viewportBounds, setViewportBounds] = useState<
    [number, number, number, number]
  >([40.68, -74.045, 40.815, -73.935]);
  const [mapZoom, setMapZoom] = useState(13);

  const { data: districtPage } = useDistricts({
    page: 0,
    size: 20,
    sort: "name,asc",
  });
  const districts = districtPage?.content ?? [];

  const { data: incidentPoints, isLoading: isLoadingIncidents } =
    useIncidentGeoPoints({
      minLat: viewportBounds[0],
      minLon: viewportBounds[1],
      maxLat: viewportBounds[2],
      maxLon: viewportBounds[3],
      severity: severityFilter === "ALL" ? undefined : severityFilter,
      districtId: selectedDistrictId ?? undefined,
    });

  const filteredIncidents = useMemo(() => {
    if (!incidentPoints) return [];
    return incidentPoints;
  }, [incidentPoints]);

  const incidentClusters = useMemo(() => {
    if (!filteredIncidents) return [];
    const resolution = Math.max(
      0.002,
      0.035 / Math.pow(2, Math.max(1, mapZoom - 10)),
    );
    const clusters = new Map<
      string,
      {
        latitude: number;
        longitude: number;
        severity: string;
        count: number;
        incidents: Incident[];
      }
    >();

    filteredIncidents.forEach((incident) => {
      const key = getClusterKey(
        incident.latitude,
        incident.longitude,
        resolution,
      );
      const existing = clusters.get(key);
      if (existing) {
        existing.count += 1;
        existing.severity = [existing.severity, incident.severity].includes(
          "CRITICAL",
        )
          ? "CRITICAL"
          : existing.severity === "HIGH" || incident.severity === "HIGH"
            ? "HIGH"
            : existing.severity === "MEDIUM" || incident.severity === "MEDIUM"
              ? "MEDIUM"
              : "LOW";
        existing.incidents.push(incident);
      } else {
        clusters.set(key, {
          latitude: incident.latitude,
          longitude: incident.longitude,
          severity: incident.severity,
          count: 1,
          incidents: [incident],
        });
      }
    });

    return Array.from(clusters.values());
  }, [filteredIncidents, mapZoom]);

  const heatmapGroups = useMemo(() => {
    const groups = new Map<
      string,
      { count: number; latitude: number; longitude: number }
    >();
    filteredIncidents.forEach((incident) => {
      const key = getClusterKey(incident.latitude, incident.longitude, 0.01);
      const existing = groups.get(key);
      if (existing) {
        existing.count += 1;
      } else {
        groups.set(key, {
          count: 1,
          latitude: incident.latitude,
          longitude: incident.longitude,
        });
      }
    });
    return Array.from(groups.values());
  }, [filteredIncidents]);

  const selectedDistrictName = districts.find(
    (district) => district.id === selectedDistrictId,
  )?.name;
  const activeIncidentCount = filteredIncidents.length;
  const hotspotScore = filteredIncidents.filter(
    (incident) => incident.severity === "CRITICAL",
  ).length;

  const styleDistrictFeature = (feature: any) => {
    const district = districts.find(
      (districtItem) => districtItem.name === feature.properties?.name,
    );
    return {
      color: "#1f2937",
      weight: 1.4,
      dashArray: "4",
      fillOpacity: 0.16,
      fillColor: getDistrictFillColor(district?.operationalRiskScore),
    };
  };

  const handleSeverityToggle = (level: (typeof severityOptions)[number]) => {
    if (level === "ALL") {
      setSeverityFilter("ALL");
      return;
    }
    setSeverityFilter(level);
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">
          Spatial Intelligence
        </h1>
        <p className="text-muted-foreground mt-2">
          Geographic operational analysis for incidents, hotspots and district
          risk.
        </p>
      </div>

      <div className="grid gap-4 xl:grid-cols-[2fr_1fr]">
        <div className="space-y-4">
          <div className="rounded-lg border border-border bg-card p-6">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">
                  Interactive map
                </p>
                <h2 className="text-xl font-semibold">Incident geography</h2>
              </div>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={() => setShowHotspots((value) => !value)}
                  className={cn(
                    "inline-flex items-center gap-2 rounded-full border px-3 py-2 text-sm transition",
                    showHotspots
                      ? "border-primary bg-primary text-primary-foreground"
                      : "border-border bg-background text-foreground hover:border-primary hover:text-primary",
                  )}
                >
                  <Layers className="h-4 w-4" />
                  {showHotspots ? "Hotspots on" : "Hotspots off"}
                </button>
              </div>
            </div>

            <div className="mt-6 h-[640px] overflow-hidden rounded-xl border border-border">
              <MapContainer
                center={defaultCenter}
                zoom={13}
                scrollWheelZoom
                className="h-full w-full"
              >
                <TileLayer
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                  url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
                />
                <GeoJSON
                  data={districtGeoJson as any}
                  style={styleDistrictFeature}
                />
                <MapInteractionHandler
                  setBounds={setViewportBounds}
                  setZoom={setMapZoom}
                />

                {showHotspots &&
                  heatmapGroups.map((group) => {
                    const intensity = Math.min(1, group.count / 5);
                    return (
                      <Circle
                        key={`${group.latitude}-${group.longitude}`}
                        center={[group.latitude, group.longitude]}
                        radius={200 + group.count * 60}
                        pathOptions={{
                          color: "rgba(220, 38, 38, 0.0)",
                          fillColor: `rgba(220, 38, 38, ${0.12 + intensity * 0.32})`,
                          fillOpacity: 0.55,
                        }}
                      />
                    );
                  })}

                {incidentClusters.map((cluster) => (
                  <CircleMarker
                    key={`${cluster.latitude}-${cluster.longitude}-${cluster.count}`}
                    center={[cluster.latitude, cluster.longitude]}
                    radius={Math.min(20, 6 + cluster.count * 2)}
                    pathOptions={{
                      color: severityColors[cluster.severity] ?? "#475569",
                      fillColor: severityColors[cluster.severity] ?? "#475569",
                      fillOpacity: 0.8,
                    }}
                  >
                    <Popup>
                      <div className="space-y-2 text-sm">
                        <p className="font-semibold">
                          {cluster.count} incident{cluster.count > 1 ? "s" : ""}
                        </p>
                        <p>Severity: {cluster.severity}</p>
                        <p>Region: {selectedDistrictName ?? "Map viewport"}</p>
                        {cluster.count === 1 && (
                          <p>{cluster.incidents[0].description}</p>
                        )}
                      </div>
                    </Popup>
                  </CircleMarker>
                ))}
              </MapContainer>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <KPICard
              label="Map incidents"
              value={isLoadingIncidents ? "Loading" : activeIncidentCount}
              unit="records"
              icon={<AlertTriangle className="h-4 w-4" />}
            />
            <KPICard
              label="Critical hotspot score"
              value={hotspotScore}
              icon={<Target className="h-4 w-4" />}
            />
          </div>
        </div>

        <aside className="space-y-4">
          <div className="rounded-lg border border-border bg-card p-6">
            <div className="flex items-center gap-3 mb-4">
              <Globe2 className="h-5 w-5 text-primary" />
              <div>
                <p className="text-sm font-medium text-muted-foreground">
                  Spatial filters
                </p>
                <h3 className="text-lg font-semibold">Geo query controls</h3>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <label className="text-sm font-medium text-foreground">
                  District
                </label>
                <select
                  value={selectedDistrictId ?? ""}
                  onChange={(event) =>
                    setSelectedDistrictId(
                      event.target.value ? Number(event.target.value) : null,
                    )
                  }
                  className="mt-2 w-full rounded-md border border-border bg-background px-3 py-2 text-sm"
                >
                  <option value="">All districts</option>
                  {districts.map((district) => (
                    <option key={district.id} value={district.id}>
                      {district.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <p className="text-sm font-medium text-foreground mb-2">
                  Severity
                </p>
                <div className="grid grid-cols-3 gap-2">
                  {severityOptions.map((level) => (
                    <button
                      key={level}
                      type="button"
                      onClick={() => handleSeverityToggle(level)}
                      className={cn(
                        "rounded-lg border px-3 py-2 text-sm font-medium transition",
                        severityFilter === level
                          ? "border-primary bg-primary text-primary-foreground"
                          : "border-border bg-background text-foreground hover:border-primary hover:text-primary",
                      )}
                    >
                      {level}
                    </button>
                  ))}
                </div>
              </div>

              <div className="rounded-lg border border-border bg-background p-4">
                <p className="text-sm font-medium text-foreground mb-3">
                  District overlay
                </p>
                <p className="text-sm text-muted-foreground">
                  District boundaries are rendered as GeoJSON overlays with
                  risk-based fill tinting.
                </p>
              </div>

              <div className="rounded-lg border border-border bg-background p-4">
                <p className="text-sm font-medium text-foreground mb-3">
                  View focus
                </p>
                <p className="text-sm text-muted-foreground">
                  The map query dynamically filters incidents inside the current
                  viewport and selected district.
                </p>
              </div>
            </div>
          </div>

          <div className="rounded-lg border border-border bg-card p-6">
            <div className="flex items-center gap-3 mb-4">
              <MapPin className="h-5 w-5 text-primary" />
              <div>
                <p className="text-sm font-medium text-muted-foreground">
                  Risk layers
                </p>
                <h3 className="text-lg font-semibold">District metrics</h3>
              </div>
            </div>
            <div className="space-y-3">
              {districts.slice(0, 4).map((district) => (
                <div
                  key={district.id}
                  className="rounded-lg border border-border p-3"
                >
                  <p className="font-semibold">{district.name}</p>
                  <p className="text-xs text-muted-foreground">
                    Operational risk: {district.operationalRiskScore.toFixed(1)}
                    %
                  </p>
                </div>
              ))}
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
};

interface MapInteractionHandlerProps {
  setBounds: (bounds: [number, number, number, number]) => void;
  setZoom: (zoom: number) => void;
}

function MapInteractionHandler({
  setBounds,
  setZoom,
}: MapInteractionHandlerProps) {
  useMapViewport(setBounds, setZoom);
  return null;
}
