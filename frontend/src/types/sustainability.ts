export type MetricType =
  | "AIR_QUALITY"
  | "EMISSIONS"
  | "WASTE_GENERATION"
  | "RECYCLING_RATE"
  | "ENERGY_CONSUMPTION"
  | "RENEWABLE_ENERGY"
  | "CONGESTION"
  | "MOBILITY_FLOW"
  | "TRANSIT_EFFICIENCY"
  | "GREEN_SPACE"
  | "WATER_USAGE";

export type MetricStatus = "GOOD" | "MODERATE" | "POOR" | "CRITICAL";

export type SustainabilityTrend = "IMPROVING" | "STABLE" | "DECLINING";

export type Rating = "A" | "B" | "C" | "D" | "F";

export interface SustainabilityMetricResponse {
  id: number;
  districtId: number;
  type: string;
  metricType: MetricType;
  value: number;
  unit: string;
  threshold: number;
  status: MetricStatus;
  source: string | null;
  timestamp: string;
}

export interface SustainabilityScoreResponse {
  id: number;
  districtId: number;
  districtName: string;
  overallScore: number;
  environmentalScore: number;
  mobilityScore: number;
  energyScore: number;
  wasteScore: number;
  rating: Rating;
  trend: SustainabilityTrend;
  trendPercentage: number;
  calculatedAt: string;
}

export interface EnvironmentalSummaryResponse {
  analysisWindowDays: number;
  totalMetrics: number;
  criticalAlerts: number;
  environmentalRiskScore: number;
  averageAirQuality: number;
  averageEmissions: number;
  averageWasteGeneration: number;
  statusDistribution: Record<string, number>;
  generatedAt: string;
}

export interface MobilitySummaryResponse {
  analysisWindowDays: number;
  metricCount: number;
  congestionEfficiency: number;
  averageCongestion: number;
  averageMobilityFlow: number;
  transportationPerformance: number;
  operationalStatus: MetricStatus;
  generatedAt: string;
}

export interface SustainabilityDashboardResponse {
  kpis: KpiCard[];
  ranking: SustainabilityScoreResponse[];
  districtComparisons: DistrictComparison[];
  trendCharts: TrendPoint[];
  environmentalAlerts: SustainabilityMetricResponse[];
  generatedAt: string;
}

export interface KpiCard {
  key: string;
  label: string;
  value: number;
  unit: string;
  status: MetricStatus;
  changePercentage: number;
}

export interface DistrictComparison {
  districtId: number;
  districtName: string;
  sustainabilityScore: number;
  environmentalScore: number;
  mobilityScore: number;
  rating: Rating;
  trend: SustainabilityTrend;
}

export interface TrendPoint {
  date: string;
  metricType: string;
  averageValue: number;
  unit: string;
}

export interface SustainabilityTrendResponse {
  districtId: number;
  districtName: string;
  analysisWindowDays: number;
  trendDirection: SustainabilityTrend;
  changePercentage: number;
  points: TrendPointDetail[];
}

export interface TrendPointDetail {
  calculatedAt: string;
  overallScore: number;
  environmentalScore: number;
  mobilityScore: number;
  energyScore: number;
  wasteScore: number;
  rating: Rating;
}

export interface SustainabilityMetricCreateRequest {
  districtId: number;
  metricType: MetricType;
  value: number;
  unit: string;
  threshold: number;
  source?: string;
}
