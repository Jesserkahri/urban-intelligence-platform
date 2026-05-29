import React, { useState } from "react";
import {
  BarChart3,
  Cloud,
  Cpu,
  Factory,
  Flame,
  Leaf,
  RotateCcw,
  Route,
  TrendingDown,
  TrendingUp,
  Truck,
} from "lucide-react";
import { KPICard } from "@components/common/KPICard";
import { AnalyticsChart } from "@components/common/AnalyticsChart";
import { Badge } from "@components/ui/Badge";
import { Button } from "@components/ui/Button";
import {
  useSustainabilityDashboard,
  useEnvironmentalSummary,
  useMobilitySummary,
  useCalculateAllScores,
} from "@hooks/useSustainability";
import { cn } from "@lib/utils";
import type {
  DistrictComparison,
  MetricStatus,
} from "@appTypes/sustainability";

const statusColor: Record<MetricStatus, string> = {
  GOOD: "text-green-500",
  MODERATE: "text-yellow-500",
  POOR: "text-orange-500",
  CRITICAL: "text-red-500",
};

const statusBg: Record<MetricStatus, string> = {
  GOOD: "bg-green-50 text-green-800 dark:bg-green-900/30 dark:text-green-300",
  MODERATE:
    "bg-yellow-50 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300",
  POOR: "bg-orange-50 text-orange-800 dark:bg-orange-900/30 dark:text-orange-300",
  CRITICAL: "bg-red-50 text-red-800 dark:bg-red-900/30 dark:text-red-300",
};

const ratingColor: Record<string, string> = {
  A: "bg-green-500",
  B: "bg-emerald-500",
  C: "bg-yellow-500",
  D: "bg-orange-500",
  F: "bg-red-500",
};

export const SustainabilityPage: React.FC = () => {
  const [environmentalDays, setEnvironmentalDays] = useState(30);
  const [mobilityDays, setMobilityDays] = useState(30);

  const { data: dashboard, isLoading: isDashboardLoading } =
    useSustainabilityDashboard();
  const { data: environmental, isLoading: isEnvironmentalLoading } =
    useEnvironmentalSummary(environmentalDays);
  const { data: mobility, isLoading: isMobilityLoading } =
    useMobilitySummary(mobilityDays);
  const calculateAllMutation = useCalculateAllScores();

  const kpis = dashboard?.kpis ?? [];
  const comparisons = dashboard?.districtComparisons ?? [];
  const trendCharts = dashboard?.trendCharts ?? [];
  const alerts = dashboard?.environmentalAlerts ?? [];

  const kpiIconMap: Record<string, React.ReactNode> = {
    overall_sustainability: <Leaf className="h-4 w-4" />,
    environmental_quality: <Cloud className="h-4 w-4" />,
    mobility_efficiency: <Route className="h-4 w-4" />,
    environmental_alerts: <Flame className="h-4 w-4" />,
    environmental_risk: <Cpu className="h-4 w-4" />,
  };

  const metricLabelMap: Record<string, string> = {
    AIR_QUALITY: "Air Quality Index",
    EMISSIONS: "CO₂ Emissions",
    WASTE_GENERATION: "Waste Generation",
    RECYCLING_RATE: "Recycling Rate",
    ENERGY_CONSUMPTION: "Energy Consumption",
    RENEWABLE_ENERGY: "Renewable Energy",
    CONGESTION: "Traffic Congestion",
    MOBILITY_FLOW: "Mobility Flow",
    TRANSIT_EFFICIENCY: "Transit Efficiency",
    GREEN_SPACE: "Green Space Coverage",
    WATER_USAGE: "Water Usage",
  };

  const trendData = trendCharts.reduce<
    Record<string, { date: string; value: number; unit: string }[]>
  >((acc, point) => {
    if (!acc[point.metricType]) acc[point.metricType] = [];
    acc[point.metricType].push({
      date: point.date,
      value: point.averageValue,
      unit: point.unit,
    });
    return acc;
  }, {});

  const chartForMetric = (metricKey: string, color: string) => {
    const data = trendData[metricKey] ?? [];
    if (data.length === 0) return null;
    return (
      <AnalyticsChart
        title={metricLabelMap[metricKey] ?? metricKey}
        data={data.slice(-30).map((d) => ({ name: d.date, value: d.value }))}
        lines={[
          {
            key: "value",
            name: metricLabelMap[metricKey] ?? metricKey,
            color,
          },
        ]}
        height={200}
      />
    );
  };

  const handleCalculateAll = () => {
    calculateAllMutation.mutate();
  };

  const getKpiValue = (value: number | undefined | null, unit: string) => {
    if (value === undefined || value === null) return "—";
    if (unit === "score" || unit === "%" || unit === "alerts") {
      return unit === "alerts"
        ? `${value}`
        : `${value.toFixed(1)}${unit === "score" ? "" : unit}`;
    }
    return `${value.toFixed(1)} ${unit}`;
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Sustainability Intelligence
          </h1>
          <p className="text-muted-foreground mt-2">
            Environmental metrics, mobility analytics, and district
            sustainability scoring.
          </p>
        </div>
        <Button
          variant="outline"
          onClick={handleCalculateAll}
          disabled={calculateAllMutation.isPending}
        >
          <RotateCcw
            className={cn(
              "mr-2 h-4 w-4",
              calculateAllMutation.isPending && "animate-spin",
            )}
          />
          {calculateAllMutation.isPending
            ? "Calculating..."
            : "Recalculate scores"}
        </Button>
      </div>

      {isDashboardLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-5 gap-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-24 rounded-lg bg-muted animate-pulse" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-5 gap-4">
          {kpis.map((kpi) => (
            <KPICard
              key={kpi.key}
              label={kpi.label}
              value={getKpiValue(kpi.value, kpi.unit)}
              unit={kpi.unit === "score" ? "pts" : undefined}
              change={kpi.changePercentage}
              trend={kpi.changePercentage >= 0 ? "up" : "down"}
              icon={kpiIconMap[kpi.key] ?? <BarChart3 className="h-4 w-4" />}
            />
          ))}
        </div>
      )}

      {/* Main grid */}
      <div className="grid gap-6 xl:grid-cols-[2fr_1fr]">
        {/* Left column */}
        <div className="space-y-6">
          {/* Environmental Metrics */}
          <div className="rounded-lg border border-border bg-card p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <p className="text-sm font-medium text-muted-foreground">
                  Environmental indicators
                </p>
                <h2 className="text-xl font-semibold">Environmental metrics</h2>
              </div>
              <select
                value={environmentalDays}
                onChange={(e) => setEnvironmentalDays(Number(e.target.value))}
                className="rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value={7}>Last 7 days</option>
                <option value={30}>Last 30 days</option>
                <option value={90}>Last 90 days</option>
              </select>
            </div>

            {isEnvironmentalLoading ? (
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                {[...Array(3)].map((_, i) => (
                  <div
                    key={i}
                    className="h-20 rounded-lg bg-muted animate-pulse"
                  />
                ))}
              </div>
            ) : environmental ? (
              <>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
                  <div className="rounded-lg border border-border p-4">
                    <div className="flex items-center gap-2 mb-2">
                      <Cloud className="h-4 w-4 text-primary" />
                      <p className="text-sm font-medium">Air Quality</p>
                    </div>
                    <p className="text-2xl font-bold">
                      {environmental.averageAirQuality.toFixed(1)}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      AQI — {environmental.totalMetrics} readings
                    </p>
                  </div>
                  <div className="rounded-lg border border-border p-4">
                    <div className="flex items-center gap-2 mb-2">
                      <Factory className="h-4 w-4 text-primary" />
                      <p className="text-sm font-medium">Emissions</p>
                    </div>
                    <p className="text-2xl font-bold">
                      {environmental.averageEmissions.toFixed(1)}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      kg CO₂e — {environmental.analysisWindowDays}-day window
                    </p>
                  </div>
                  <div className="rounded-lg border border-border p-4">
                    <div className="flex items-center gap-2 mb-2">
                      <Truck className="h-4 w-4 text-primary" />
                      <p className="text-sm font-medium">Waste</p>
                    </div>
                    <p className="text-2xl font-bold">
                      {environmental.averageWasteGeneration.toFixed(1)}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      tons —{" "}
                      {environmental.criticalAlerts > 0
                        ? `${environmental.criticalAlerts} critical alerts`
                        : "All clear"}
                    </p>
                  </div>
                </div>

                <div className="rounded-lg border border-border p-4 mb-4">
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-sm font-medium">
                      Environmental Risk Score
                    </p>
                    <span
                      className={cn(
                        "text-sm font-semibold",
                        statusColor[
                          environmental.environmentalRiskScore >= 30
                            ? "CRITICAL"
                            : environmental.environmentalRiskScore >= 15
                              ? "POOR"
                              : environmental.environmentalRiskScore >= 5
                                ? "MODERATE"
                                : "GOOD"
                        ],
                      )}
                    >
                      {environmental.environmentalRiskScore.toFixed(1)}%
                    </span>
                  </div>
                  <div className="h-2 w-full rounded-full bg-muted overflow-hidden">
                    <div
                      className={cn(
                        "h-full rounded-full transition-all",
                        environmental.environmentalRiskScore >= 30
                          ? "bg-red-500"
                          : environmental.environmentalRiskScore >= 15
                            ? "bg-orange-500"
                            : environmental.environmentalRiskScore >= 5
                              ? "bg-yellow-500"
                              : "bg-green-500",
                      )}
                      style={{
                        width: `${Math.min(environmental.environmentalRiskScore, 100)}%`,
                      }}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {chartForMetric("AIR_QUALITY", "#06b6d4")}
                  {chartForMetric("EMISSIONS", "#ef4444")}
                  {chartForMetric("WASTE_GENERATION", "#eab308")}
                  {chartForMetric("GREEN_SPACE", "#22c55e")}
                </div>
              </>
            ) : (
              <p className="text-muted-foreground text-sm py-4">
                No environmental data available yet. Record sustainability
                metrics or recalculate scores.
              </p>
            )}
          </div>

          {/* Mobility Intelligence */}
          <div className="rounded-lg border border-border bg-card p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <p className="text-sm font-medium text-muted-foreground">
                  Transportation & mobility
                </p>
                <h2 className="text-xl font-semibold">Mobility intelligence</h2>
              </div>
              <select
                value={mobilityDays}
                onChange={(e) => setMobilityDays(Number(e.target.value))}
                className="rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value={7}>Last 7 days</option>
                <option value={30}>Last 30 days</option>
                <option value={90}>Last 90 days</option>
              </select>
            </div>

            {isMobilityLoading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {[...Array(2)].map((_, i) => (
                  <div
                    key={i}
                    className="h-20 rounded-lg bg-muted animate-pulse"
                  />
                ))}
              </div>
            ) : mobility ? (
              <>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                  <div className="rounded-lg border border-border p-4">
                    <div className="flex items-center gap-2 mb-2">
                      <Route className="h-4 w-4 text-primary" />
                      <p className="text-sm font-medium">
                        Congestion Efficiency
                      </p>
                    </div>
                    <p className="text-2xl font-bold">
                      {mobility.congestionEfficiency.toFixed(1)}%
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Avg congestion: {mobility.averageCongestion.toFixed(1)}
                    </p>
                  </div>
                  <div className="rounded-lg border border-border p-4">
                    <div className="flex items-center gap-2 mb-2">
                      <BarChart3 className="h-4 w-4 text-primary" />
                      <p className="text-sm font-medium">
                        Transportation Performance
                      </p>
                    </div>
                    <p className="text-2xl font-bold">
                      {mobility.transportationPerformance.toFixed(1)}%
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Status:{" "}
                      <span
                        className={cn(
                          "font-medium",
                          statusColor[mobility.operationalStatus],
                        )}
                      >
                        {mobility.operationalStatus}
                      </span>
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {chartForMetric("CONGESTION", "#f97316")}
                  {chartForMetric("MOBILITY_FLOW", "#06b6d4")}
                  {chartForMetric("TRANSIT_EFFICIENCY", "#22c55e")}
                </div>
              </>
            ) : (
              <p className="text-muted-foreground text-sm py-4">
                No mobility data available yet.
              </p>
            )}
          </div>
        </div>

        {/* Right sidebar */}
        <aside className="space-y-4">
          {/* District Ranking */}
          <div className="rounded-lg border border-border bg-card p-6">
            <div className="flex items-center gap-3 mb-4">
              <Leaf className="h-5 w-5 text-primary" />
              <div>
                <p className="text-sm font-medium text-muted-foreground">
                  District ranking
                </p>
                <h3 className="text-lg font-semibold">Sustainability scores</h3>
              </div>
            </div>
            <div className="space-y-3">
              {comparisons.length > 0 ? (
                comparisons.map((district: DistrictComparison) => (
                  <div
                    key={district.districtId}
                    className="rounded-lg border border-border p-3"
                  >
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <p className="font-semibold text-sm truncate">
                        {district.districtName}
                      </p>
                      <div className="flex items-center gap-1">
                        <span
                          className={cn(
                            "inline-flex h-6 w-6 items-center justify-center rounded-full text-xs font-bold text-white",
                            ratingColor[district.rating] ?? "bg-gray-500",
                          )}
                        >
                          {district.rating}
                        </span>
                        {district.trend === "IMPROVING" ? (
                          <TrendingUp className="h-3.5 w-3.5 text-green-500" />
                        ) : district.trend === "DECLINING" ? (
                          <TrendingDown className="h-3.5 w-3.5 text-red-500" />
                        ) : null}
                      </div>
                    </div>
                    <div className="h-1.5 w-full rounded-full bg-muted overflow-hidden">
                      <div
                        className={cn(
                          "h-full rounded-full transition-all",
                          district.sustainabilityScore >= 80
                            ? "bg-green-500"
                            : district.sustainabilityScore >= 65
                              ? "bg-yellow-500"
                              : district.sustainabilityScore >= 50
                                ? "bg-orange-500"
                                : "bg-red-500",
                        )}
                        style={{
                          width: `${district.sustainabilityScore}%`,
                        }}
                      />
                    </div>
                    <div className="flex justify-between mt-1">
                      <p className="text-xs text-muted-foreground">
                        Environmental: {district.environmentalScore.toFixed(1)}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Mobility: {district.mobilityScore.toFixed(1)}
                      </p>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-sm text-muted-foreground py-2">
                  No ranking data available. Click "Recalculate scores" to
                  generate district sustainability rankings.
                </p>
              )}
            </div>
          </div>

          {/* Environmental Alerts */}
          <div className="rounded-lg border border-border bg-card p-6">
            <div className="flex items-center gap-3 mb-4">
              <Flame className="h-5 w-5 text-red-500" />
              <div>
                <p className="text-sm font-medium text-muted-foreground">
                  Real-time monitoring
                </p>
                <h3 className="text-lg font-semibold">Environmental alerts</h3>
              </div>
            </div>
            {alerts.length > 0 ? (
              <div className="space-y-2">
                {alerts.slice(0, 10).map((alert) => (
                  <div
                    key={alert.id}
                    className="rounded-lg border border-destructive/30 bg-destructive/5 p-3"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <div className="min-w-0">
                        <p className="font-medium text-sm truncate">
                          {metricLabelMap[alert.metricType] ?? alert.metricType}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          District {alert.districtId} • Value:{" "}
                          {alert.value.toFixed(1)} {alert.unit}
                        </p>
                      </div>
                      <Badge
                        className={cn(
                          "shrink-0",
                          statusBg[alert.status as MetricStatus],
                        )}
                      >
                        {alert.status}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center gap-2 py-4 text-center">
                <Leaf className="h-8 w-8 text-green-500" />
                <p className="text-sm text-muted-foreground">
                  No critical alerts. All metrics are within acceptable
                  thresholds.
                </p>
              </div>
            )}
          </div>
        </aside>
      </div>
    </div>
  );
};
