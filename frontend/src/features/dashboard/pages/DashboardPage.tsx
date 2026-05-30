import React from "react";
import { AlertTriangle, TrendingUp, MapPin } from "lucide-react";
import { KPICard } from "@components/common/KPICard";
import { useDashboardInsights } from "@hooks/useAnalytics";
import { useLiveOperations } from "@context/liveOperations";
import { useDistricts } from "@hooks/useDistricts";
import { useRecentIncidents } from "@hooks/useIncidents";
import { useLiveDashboardSnapshot } from "@hooks/useOperations";
import { formatDate } from "@lib/utils";

export const DashboardPage: React.FC = () => {
  const { data: districts } = useDistricts({
    page: 0,
    size: 20,
    sort: "name,asc",
  });
  const { data: recentIncidents } = useRecentIncidents();
  const { data: dashboardInsights, isLoading: isDashboardLoading } =
    useDashboardInsights();
  const {
    connected,
    latestEvent,
    snapshotGas Leak: streamedSnapshot,
  } = useLiveOperations();
  const { data: polledSnapshot } = useLiveDashboardSnapshot();

  const liveSnapshot = streamedSnapshot ?? polledSnapshot;

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
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
          value={
            liveSnapshot?.activeIncidents ?? recentIncidents?.length ?? "--"
          }
          icon={<AlertTriangle className="h-4 w-4" />}
        />
        <KPICard
          label="Districts Monitored"
          value={districts?.totalElements ?? "—"}
          icon={<MapPin className="h-4 w-4" />}
        />
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
    </div>
  );
};
