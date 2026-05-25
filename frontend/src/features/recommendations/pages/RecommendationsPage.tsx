import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import { Badge } from "@components/ui/Badge";
import { PRIORITY_COLORS } from "@lib/utils";

const recommendations = [
  {
    id: 1,
    title: "Optimize traffic signals",
    description: "Downtown area showing 35% congestion during peak hours",
    priority: "HIGH" as const,
    district: "Downtown",
  },
  {
    id: 2,
    title: "Increase air quality monitoring",
    description: "North district exceeded PM2.5 thresholds 5 times this month",
    priority: "HIGH" as const,
    district: "North District",
  },
  {
    id: 3,
    title: "Energy efficiency audit",
    description: "Public buildings showing 20% higher consumption than average",
    priority: "MEDIUM" as const,
    district: "Central",
  },
  {
    id: 4,
    title: "Parking management review",
    description: "South district parking at 95% capacity most weekdays",
    priority: "MEDIUM" as const,
    district: "South District",
  },
];

export const RecommendationsPage: React.FC = () => {
  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Recommendations</h1>
        <p className="text-muted-foreground mt-2">
          AI-generated operational recommendations
        </p>
      </div>

      <div className="grid gap-4">
        {recommendations.map((rec) => (
          <Card key={rec.id}>
            <CardContent className="pt-6">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1">
                  <h3 className="font-semibold text-lg">{rec.title}</h3>
                  <p className="text-sm text-muted-foreground mt-1">
                    {rec.description}
                  </p>
                  <p className="text-xs text-muted-foreground mt-2">
                    District: {rec.district}
                  </p>
                </div>
                <Badge className={PRIORITY_COLORS[rec.priority]}>
                  {rec.priority}
                </Badge>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
};
