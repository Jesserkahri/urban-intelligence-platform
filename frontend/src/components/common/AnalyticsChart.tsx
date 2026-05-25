import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

interface ChartData {
  name: string;
  [key: string]: string | number;
}

interface AnalyticsChartProps {
  title: string;
  data: ChartData[];
  lines: {
    key: string;
    name: string;
    color: string;
  }[];
  height?: number;
}

export const AnalyticsChart: React.FC<AnalyticsChartProps> = ({
  title,
  data,
  lines,
  height = 300,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={height}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Legend />
            {lines.map((line) => (
              <Line
                key={line.key}
                type="monotone"
                dataKey={line.key}
                name={line.name}
                stroke={line.color}
                strokeWidth={2}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
};
