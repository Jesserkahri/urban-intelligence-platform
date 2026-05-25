import React from "react";
import { KPICard } from "@components/common/KPICard";
import { AnalyticsChart } from "@components/common/AnalyticsChart";
import { RecentIncidents } from "@components/common/RecentIncidents";
import { AlertTriangle, TrendingUp, Zap, MapPin } from "lucide-react";

// Mock data
const mockKPIs = [
  {
    label: "Active Incidents",
    value: 24,
    change: 12,
    trend: "up" as const,
    icon: <AlertTriangle className="h-4 w-4" />,
  },
  {
    label: "Avg Risk Score",
    value: "65.4",
    unit: "%",
    change: -5,
    trend: "down" as const,
    icon: <TrendingUp className="h-4 w-4" />,
  },
  {
    label: "Resolved Today",
    value: 12,
    change: 8,
    trend: "up" as const,
    icon: <Zap className="h-4 w-4" />,
  },
  {
    label: "Districts Monitored",
    value: 42,
    icon: <MapPin className="h-4 w-4" />,
  },
];

const mockChartData = [
  { name: "Jan", incidents: 45, riskScore: 62 },
  { name: "Feb", incidents: 52, riskScore: 65 },
  { name: "Mar", incidents: 48, riskScore: 61 },
  { name: "Apr", incidents: 61, riskScore: 68 },
  { name: "May", incidents: 55, riskScore: 64 },
  { name: "Jun", incidents: 42, riskScore: 59 },
];

const mockIncidents = [
  {
    id: 1,
    title: "Traffic congestion detected",
    districtName: "Downtown",
    severity: "HIGH" as const,
    status: "OPEN",
    createdAt: new Date().toISOString(),
  },
  {
    id: 2,
    title: "Air quality threshold exceeded",
    districtName: "North District",
    severity: "CRITICAL" as const,
    status: "IN_PROGRESS",
    createdAt: new Date(Date.now() - 86400000).toISOString(),
  },
  {
    id: 3,
    title: "Parking occupancy high",
    districtName: "Central",
    severity: "MEDIUM" as const,
    status: "OPEN",
    createdAt: new Date(Date.now() - 172800000).toISOString(),
  },
];

export const DashboardPage: React.FC = () => {
  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-foreground">Dashboard</h1>
        <p className="text-muted-foreground mt-2">
          Urban intelligence and operational metrics
        </p>
      </div>

      {/* KPI Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {mockKPIs.map((kpi) => (
          <KPICard key={kpi.label} {...kpi} />
        ))}
      </div>

      {/* Charts and Incidents */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <AnalyticsChart
            title="Incidents & Risk Trends"
            data={mockChartData}
            lines={[
              { key: "incidents", name: "Incidents", color: "#ef4444" },
              { key: "riskScore", name: "Risk Score", color: "#f59e0b" },
            ]}
            height={300}
          />
        </div>

        <RecentIncidents incidents={mockIncidents} />
      </div>
    </div>
  );
};
