import React from "react";
import { AlertTriangle, TrendingUp, Zap, MapPin } from "lucide-react";
import { KPICard } from "@components/common/KPICard";
import { AnalyticsChart } from "@components/common/AnalyticsChart";
import { RecentIncidents } from "@components/common/RecentIncidents";
import {
  useDailyTrends,
  useDistrictRiskRanking,
  useHotspotRankings,
} from "@hooks/useAnalytics";
import { useDistricts } from "@hooks/useDistricts";
import { useRecentIncidents } from "@hooks/useIncidents";
import { formatDate } from "@lib/utils";
import { DistrictRiskRankingResponse } from "@appTypes/api";

export const DashboardPage: React.FC = () => {
  const { data: dailyTrend } = useDailyTrends();
  const { data: riskRanking, isLoading: isRiskLoading } =
    useDistrictRiskRanking();
  const { data: hotspots, isLoading: isHotspotLoading } = useHotspotRankings(4);
  const { data: districts } = useDistricts({
    page: 0,
    size: 20,
    sort: "name,asc",
  });
  const { data: recentIncidents, isLoading: isRecentLoading } =
    useRecentIncidents();

  const averageRiskScore =
    riskRanking && riskRanking.length
      ? (
          riskRanking.reduce((sum, item) => sum + item.riskScore, 0) /
          riskRanking.length
        ).toFixed(1)
      : "0.0";

  const resolvedToday = dailyTrend?.dailyData?.length
    ? dailyTrend.dailyData[dailyTrend.dailyData.length - 1].resolvedCount
    : 0;

  const analyticsData =
    dailyTrend?.dailyData?.map((daily) => ({
      name: formatDate(daily.date),
      incidents: daily.incidentCount,
      resolved: daily.resolvedCount,
    })) ?? [];

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Dashboard</h1>
        <p className="text-muted-foreground mt-2">
          Urban intelligence and operational metrics.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        <KPICard
          label="Active Incidents"
          value={recentIncidents?.length ?? "—"}
          change={dailyTrend ? dailyTrend.growthPercentage : undefined}
          trend={(dailyTrend?.growthPercentage ?? 0) >= 0 ? "up" : "down"}
          icon={<AlertTriangle className="h-4 w-4" />}
        />
        <KPICard
          label="Avg Risk Score"
          value={averageRiskScore}
          unit="%"
          icon={<TrendingUp className="h-4 w-4" />}
        />
        <KPICard
          label="Resolved Today"
          value={resolvedToday}
          change={dailyTrend?.growthPercentage}
          trend={(dailyTrend?.growthPercentage ?? 0) >= 0 ? "up" : "down"}
          icon={<Zap className="h-4 w-4" />}
        />
        <KPICard
          label="Districts Monitored"
          value={districts?.totalElements ?? "—"}
          icon={<MapPin className="h-4 w-4" />}
        />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-[2fr_1fr] gap-6">
        <AnalyticsChart
          title="Daily Incident Trends"
          data={analyticsData}
          lines={[
            { key: "incidents", name: "Incidents", color: "#ef4444" },
            { key: "resolved", name: "Resolved", color: "#10b981" },
          ]}
          height={340}
        />

        <div className="space-y-4">
          <div className="rounded-lg border border-border bg-card p-6">
            <div className="flex items-center justify-between gap-4 mb-4">
              <div>
                <p className="text-sm font-medium text-muted-foreground">
                  Top hotspots
                </p>
                <h2 className="text-xl font-semibold">Critical districts</h2>
              </div>
            </div>
            {isHotspotLoading ? (
              <div className="space-y-3">
                {[...Array(4)].map((_, index) => (
                  <div
                    key={index}
                    className="h-12 rounded-md bg-muted animate-pulse"
                  />
                ))}
              </div>
            ) : (
              <div className="space-y-3">
                {hotspots?.map((hotspot, index) => (
                  <div
                    key={hotspot.districtId ?? hotspot.rank ?? index}
                    className="rounded-lg border border-border p-4"
                  >
                    <div className="flex items-center justify-between gap-4">
                      <div>
                        <p className="font-semibold">{hotspot.districtName}</p>
                        <p className="text-sm text-muted-foreground">
                          {hotspot.incidentCount ?? "0"} incidents •{" "}
                          {(hotspot.unresolvedRatio ?? 0).toFixed(0)}%
                          unresolved
                        </p>
                      </div>
                      <div className="text-right text-sm">
                        <p className="font-semibold">
                          {hotspot.hotspotScore?.toFixed(1) ?? "0.0"}
                        </p>
                        <p className="text-muted-foreground">
                          {hotspot.riskIntensity ??
                            hotspot.criticalityLevel ??
                            "—"}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="rounded-lg border border-border bg-card p-6">
            <p className="text-sm font-medium text-muted-foreground">
              Risk ranking
            </p>
            <h2 className="text-xl font-semibold">District scores</h2>
            <div className="mt-4 space-y-3">
              {(isRiskLoading
                ? (Array.from({
                    length: 3,
                  }) as Partial<DistrictRiskRankingResponse>[])
                : ((riskRanking as Partial<DistrictRiskRankingResponse>[]) ??
                  [])
              ).map((district, index) => (
                <div
                  key={district?.districtId ?? index}
                  className="flex items-center justify-between rounded-lg border border-border p-3"
                >
                  <div>
                    <p className="font-medium">
                      {district?.districtName ?? "Loading"}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {district?.incidentCount ?? 0} incidents
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold">
                      {district?.riskScore?.toFixed(1) ?? "—"}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {district?.riskLevel ?? "—"}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <RecentIncidents
        incidents={recentIncidents ?? []}
        isLoading={isRecentLoading}
      />
    </div>
  );
};
