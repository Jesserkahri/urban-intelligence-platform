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
  useAnomalies,
  useCategoryTrends,
  useDailyTrends,
  useDistrictRiskRanking,
  useForecasts,
  useRiskExplanations,
  useSpatialRisk,
} from "@hooks/useAnalytics";

const CHART_COLORS = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6"];

export const AnalyticsPage: React.FC = () => {
  const { data: categoryTrends, isLoading: isCategoriesLoading } =
    useCategoryTrends();
  const { data: dailyTrends, isLoading: isDailyLoading } = useDailyTrends();
  const { data: riskRanking, isLoading: isRiskLoading } =
    useDistrictRiskRanking();
  const { data: anomalies } = useAnomalies();
  const { data: forecasts } = useForecasts(7);
  const { data: spatialRisk } = useSpatialRisk();
  const { data: riskExplanations } = useRiskExplanations();

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

  const topForecast = forecasts?.[0];
  const topRiskExplanation = riskExplanations?.[0];
  const topSpatialRisk = spatialRisk?.[0];

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
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

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Statistical anomalies</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {(anomalies ?? []).slice(0, 3).map((anomaly) => (
                <div key={`${anomaly.districtId}-${anomaly.date}`}>
                  <div className="flex items-center justify-between gap-3">
                    <p className="font-medium">{anomaly.districtName}</p>
                    <p className="text-sm font-semibold">
                      {anomaly.confidence.toFixed(0)}%
                    </p>
                  </div>
                  <div className="mt-2 h-2 rounded bg-muted">
                    <div
                      className="h-2 rounded bg-destructive"
                      style={{ width: `${Math.min(anomaly.confidence, 100)}%` }}
                    />
                  </div>
                  <p className="mt-2 text-sm text-muted-foreground">
                    Actual {anomaly.actualValue}, expected{" "}
                    {anomaly.expectedValue}. {anomaly.explanation}
                  </p>
                </div>
              ))}
              {(anomalies ?? []).length === 0 && (
                <p className="text-sm text-muted-foreground">
                  No districts currently exceed the 2 standard deviation anomaly
                  threshold.
                </p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>7-day forecast</CardTitle>
          </CardHeader>
          <CardContent>
            {topForecast ? (
              <div className="space-y-4">
                <div>
                  <p className="text-sm text-muted-foreground">
                    Highest predicted district
                  </p>
                  <p className="text-2xl font-semibold">
                    {topForecast.districtName}
                  </p>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="rounded-lg border border-border p-3">
                    <p className="text-xs text-muted-foreground">Incidents</p>
                    <p className="text-xl font-semibold">
                      {topForecast.predictedIncidents}
                    </p>
                  </div>
                  <div className="rounded-lg border border-border p-3">
                    <p className="text-xs text-muted-foreground">Confidence</p>
                    <p className="text-xl font-semibold">
                      {topForecast.confidence.toFixed(0)}%
                    </p>
                  </div>
                </div>
                <p className="text-sm text-muted-foreground">
                  {topForecast.explanation}
                </p>
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">
                Forecasts are waiting for incident history.
              </p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Spatial risk</CardTitle>
          </CardHeader>
          <CardContent>
            {topSpatialRisk ? (
              <div className="space-y-4">
                <div>
                  <p className="text-sm text-muted-foreground">
                    Highest spatial propagation
                  </p>
                  <p className="text-2xl font-semibold">
                    {topSpatialRisk.districtName}
                  </p>
                </div>
                <div className="h-2 rounded bg-muted">
                  <div
                    className="h-2 rounded bg-primary"
                    style={{
                      width: `${Math.min(topSpatialRisk.spatialRisk, 100)}%`,
                    }}
                  />
                </div>
                <p className="text-sm text-muted-foreground">
                  Spatial risk {topSpatialRisk.spatialRisk.toFixed(1)} with{" "}
                  {topSpatialRisk.neighboringImpact.toFixed(1)} neighboring
                  impact. Neighbors:{" "}
                  {topSpatialRisk.neighboringDistricts.join(", ") || "none"}.
                </p>
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">
                Spatial risk requires district data.
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Risk explanation</CardTitle>
        </CardHeader>
        <CardContent>
          {topRiskExplanation ? (
            <div className="space-y-4">
              <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">
                    {topRiskExplanation.districtName}
                  </p>
                  <p className="text-2xl font-semibold">
                    Risk {topRiskExplanation.riskScore.toFixed(1)} -{" "}
                    {topRiskExplanation.trendDirection}
                  </p>
                </div>
                <p className="text-sm text-muted-foreground">
                  Confidence {topRiskExplanation.confidence.toFixed(0)}%
                </p>
              </div>
              <div className="grid gap-3 md:grid-cols-2">
                {topRiskExplanation.contributingFactors.map((factor) => (
                  <div
                    key={factor.name}
                    className="rounded-lg border border-border p-4"
                  >
                    <div className="flex items-center justify-between gap-3">
                      <p className="font-medium">{factor.name}</p>
                      <p className="text-sm font-semibold">
                        +{factor.contribution.toFixed(1)}
                      </p>
                    </div>
                    <p className="mt-2 text-sm text-muted-foreground">
                      Weight {(factor.weight * 100).toFixed(0)}%.{" "}
                      {factor.explanation}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">
              Risk explanations are waiting for district and incident data.
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
