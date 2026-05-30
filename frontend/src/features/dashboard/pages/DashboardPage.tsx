import React from "react";
import { AlertTriangle, TrendingUp, Zap, MapPin } from "lucide-react";
import { KPICard } from "@components/common/KPICard";
import { AnalyticsChart } from "@components/common/AnalyticsChart";
import { RecentIncidents } from "@components/common/RecentIncidents";
import {
  useDashboardInsights,
  useDailyTrends,
  useDistrictRiskRanking,
  useHotspotRankings,
} from "@hooks/useAnalytics";
import { useLiveOperations } from "@context/liveOperations";
import { useDistricts } from "@hooks/useDistricts";
import { useRecentIncidents } from "@hooks/useIncidents";
import { useLiveDashboardSnapshot } from "@hooks/useOperations";
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
  const { data: dashboardInsights, isLoading: isDashboardLoading } =
    useDashboardInsights();
  const { connected, latestEvent, snapshot: streamedSnapshot } =
    useLiveOperations();
  const { data: polledSnapshot } = useLiveDashboardSnapshot();

  const liveSnapshot = streamedSnapshot ?? polledSnapshot;

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

      <div className="rounded-lg border border-border bg-card p-4">
        <div className="grid gap-4 md:grid-cols-[1fr_repeat(3,auto)] md:items-center">
          <div>
            <p className="text-sm font-medium text-muted-foreground">
              Live operations stream
            </p>
            <p className="text-lg font-semibold">
              {connected ? "Connected" : "Reconnecting"}
            </p>
            {latestEvent && (
              <p className="mt-1 text-sm text-muted-foreground">
                {latestEvent.title} - {formatDate(latestEvent.occurredAt)}
              </p>
            )}
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Active</p>
            <p className="text-2xl font-semibold">
              {liveSnapshot?.activeIncidents ?? "--"}
            </p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Critical 24h</p>
            <p className="text-2xl font-semibold">
              {liveSnapshot?.criticalIncidents24h ?? "--"}
            </p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Unread alerts</p>
            <p className="text-2xl font-semibold">
              {liveSnapshot?.unreadNotifications ?? "--"}
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        <KPICard
          label="Active Incidents"
          value={liveSnapshot?.activeIncidents ?? recentIncidents?.length ?? "--"}
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

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <KPICard
          label="24h Incidents"
          value={dashboardInsights?.health?.totalIncidents24h ?? "—"}
          change={dashboardInsights?.trendSummary?.growthPercentage}
          trend={
            (dashboardInsights?.trendSummary?.growthPercentage ?? 0) >= 0
              ? "up"
              : "down"
          }
          icon={<AlertTriangle className="h-4 w-4" />}
        />
        <KPICard
          label="Active Alerts"
          value={liveSnapshot?.alerts24h ?? dashboardInsights?.alerts?.length ?? "--"}
          unit="items"
          icon={<Zap className="h-4 w-4" />}
        />
        <KPICard
          label="Trend Signal"
          value={dashboardInsights?.trendSummary?.trendDirection ?? "—"}
          icon={<TrendingUp className="h-4 w-4" />}
        />
      </div>

      <div className="rounded-lg border border-border bg-card p-6">
        <div className="flex items-center justify-between gap-4 mb-4">
          <div>
            <p className="text-sm font-medium text-muted-foreground">
              Operational intelligence
            </p>
            <h2 className="text-xl font-semibold">Insight summary</h2>
          </div>
        </div>
        {isDashboardLoading ? (
          <div className="space-y-3">
            {[...Array(3)].map((_, index) => (
              <div
                key={index}
                className="h-12 rounded-md bg-muted animate-pulse"
              />
            ))}
          </div>
        ) : (
          <div className="space-y-4">
            {dashboardInsights?.intelligenceCards?.map((card) => (
              <div
                key={card.title}
                className="rounded-lg border border-border p-4"
              >
                <p className="text-sm font-medium text-foreground">
                  {card.title}
                </p>
                <p className="text-sm text-muted-foreground mt-2">
                  {card.detail}
                </p>
                <p className="text-xs text-muted-foreground mt-2">
                  Severity: {card.severity}
                </p>
              </div>
            ))}
          </div>
        )}
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

      <div className="grid grid-cols-1 xl:grid-cols-[1.5fr_1fr] gap-6">
        <div className="rounded-lg border border-border bg-card p-6">
          <div className="flex items-center justify-between gap-4 mb-4">
            <div>
              <p className="text-sm font-medium text-muted-foreground">
                Recommendations
              </p>
              <h2 className="text-xl font-semibold">Priority actions</h2>
            </div>
          </div>
          {isDashboardLoading ? (
            <div className="space-y-3">
              {[...Array(3)].map((_, index) => (
                <div
                  key={index}
                  className="h-12 rounded-md bg-muted animate-pulse"
                />
              ))}
            </div>
          ) : (
            <div className="space-y-3">
              {dashboardInsights?.recommendations?.map((recommendation) => (
                <div
                  key={recommendation.title}
                  className="rounded-lg border border-border p-4"
                >
                  <p className="font-semibold">{recommendation.title}</p>
                  <p className="text-sm text-muted-foreground mt-2">
                    {recommendation.rationale}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>

        <RecentIncidents
          incidents={recentIncidents ?? []}
          isLoading={isRecentLoading}
        />
      </div>
    </div>
  );
};
