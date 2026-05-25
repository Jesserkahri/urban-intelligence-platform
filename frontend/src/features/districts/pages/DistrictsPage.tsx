import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import { District } from "@types/api";

const mockDistricts: District[] = [
  {
    id: 1,
    name: "Downtown",
    population: 125000,
    sustainabilityScore: 72.5,
    operationalRiskScore: 58.3,
    createdAt: "2024-01-15",
    updatedAt: "2024-05-25",
  },
  {
    id: 2,
    name: "North District",
    population: 89000,
    sustainabilityScore: 68.2,
    operationalRiskScore: 72.1,
    createdAt: "2024-01-15",
    updatedAt: "2024-05-25",
  },
  {
    id: 3,
    name: "South District",
    population: 156000,
    sustainabilityScore: 65.8,
    operationalRiskScore: 65.5,
    createdAt: "2024-01-15",
    updatedAt: "2024-05-25",
  },
  {
    id: 4,
    name: "East District",
    population: 102000,
    sustainabilityScore: 74.3,
    operationalRiskScore: 52.1,
    createdAt: "2024-01-15",
    updatedAt: "2024-05-25",
  },
];

export const DistrictsPage: React.FC = () => {
  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Districts</h1>
        <p className="text-muted-foreground mt-2">
          Manage urban districts and their metrics
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {mockDistricts.map((district) => (
          <Card key={district.id}>
            <CardHeader>
              <CardTitle>{district.name}</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Population</span>
                  <span className="font-medium">
                    {district.population.toLocaleString()}
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Sustainability</span>
                  <span className="font-medium">
                    {district.sustainabilityScore.toFixed(1)}%
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Risk Score</span>
                  <span className="font-medium">
                    {district.operationalRiskScore.toFixed(1)}%
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
};
