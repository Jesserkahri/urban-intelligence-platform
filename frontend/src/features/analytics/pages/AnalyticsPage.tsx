import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
} from "recharts";
import {
  useCategoryTrends,
  useDailyTrends,
  useDistrictRiskRanking,
} from "@hooks/useAnalytics";

const CHART_COLORS = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6"];

export const AnalyticsPage: React.FC = () => {
  const { data: categoryTrends, isLoading: isCategoriesLoading } =
    useCategoryTrends();
  const { data: dailyTrends, isLoading: isDailyLoading } = useDailyTrends();
  const { data: riskRanking, isLoading: isRiskLoading } =
    useDistrictRiskRanking();

  const categoryData =
    categoryTrends?.categoryData.map((item) => ({
      name: item.category,
      value: item.count,
    })) ?? [];

  const riskData =
    riskRanking?.slice(0, 6).map((item) => ({
      name: item.districtName,
      riskScore: item.riskScore,
    })) ?? [];

  const dailyData =
    dailyTrends?.dailyData.map((item) => ({
      date: item.date,
      incidents: item.incidentCount,
      resolved: item.resolvedCount,
    })) ?? [];

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Analytics</h1>
        <p className="text-muted-foreground mt-2">
          Urban operations and incident analytics.
        </p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Daily incident trends</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={320}>
              <LineChart data={dailyData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="incidents"
                  stroke="#ef4444"
                  strokeWidth={2}
                />
                <Line
                  type="monotone"
                  dataKey="resolved"
                  stroke="#10b981"
                  strokeWidth={2}
                />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Risk ranking</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={320}>
              <BarChart data={riskData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="riskScore" fill="#4338ca" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Category breakdown</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={320}>
              <PieChart>
                <Pie
                  data={categoryData}
                  cx="50%"
                  cy="50%"
                  outerRadius={110}
                  dataKey="value"
                  label={({ name, value }) => `${name}: ${value}`}
                >
                  {categoryData.map((_entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={CHART_COLORS[index % CHART_COLORS.length]}
                    />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Operational summary</CardTitle>
          </CardHeader>
          <CardContent>
            {isDailyLoading || isCategoriesLoading || isRiskLoading ? (
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
                <div className="rounded-lg border border-border p-4">
                  <p className="text-sm text-muted-foreground">
                    Total incidents last period
                  </p>
                  <p className="text-xl font-semibold">
                    {dailyTrends?.totalIncidents ?? "—"}
                  </p>
                </div>
                <div className="rounded-lg border border-border p-4">
                  <p className="text-sm text-muted-foreground">
                    Average daily incidents
                  </p>
                  <p className="text-xl font-semibold">
                    {dailyTrends?.averageDaily ?? "—"}
                  </p>
                </div>
                <div className="rounded-lg border border-border p-4">
                  <p className="text-sm text-muted-foreground">Top category</p>
                  <p className="text-xl font-semibold">
                    {categoryTrends?.topCategory ?? "—"}
                  </p>
                </div>
                <div className="rounded-lg border border-border p-4">
                  <p className="text-sm text-muted-foreground">
                    Highest risk district
                  </p>
                  <p className="text-xl font-semibold">
                    {riskRanking?.[0]?.districtName ?? "—"}
                  </p>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
