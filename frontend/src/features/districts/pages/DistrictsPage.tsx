import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import { useDistricts } from "@hooks/useDistricts";

export const DistrictsPage: React.FC = () => {
  const { data, isLoading, error } = useDistricts({
    page: 0,
    size: 20,
    sort: "operationalRiskScore,desc",
  });

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      {error ? (
        <div className="rounded-lg border border-destructive bg-destructive/10 p-6 text-sm text-destructive">
          Unable to load district data. Please check your connection.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {isLoading
            ? [...Array(4)].map((_, index) => (
                <Card key={index}>
                  <CardContent>
                    <div className="space-y-3">
                      <div className="h-5 w-3/4 rounded bg-muted animate-pulse" />
                      <div className="h-4 w-1/2 rounded bg-muted animate-pulse" />
                      <div className="h-4 w-1/3 rounded bg-muted animate-pulse" />
                    </div>
                  </CardContent>
                </Card>
              ))
            : data?.content.map((district) => (
                <Card key={district.id}>
                  <CardHeader>
                    <CardTitle>{district.name}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">
                          Population
                        </span>
                        <span className="font-medium">
                          {district.population?.toLocaleString() ?? "—"}
                        </span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">
                          Sustainability
                        </span>
                        <span className="font-medium">
                          {district.sustainabilityScore?.toFixed(1) ?? "—"}%
                        </span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">
                          Risk Score
                        </span>
                        <span className="font-medium">
                          {district.operationalRiskScore?.toFixed(1) ?? "—"}%
                        </span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Incidents</span>
                        <span className="font-medium">
                          {district.incidentCount ?? 0}
                        </span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">
                          Recommendations
                        </span>
                        <span className="font-medium">
                          {district.recommendationCount ?? 0}
                        </span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
        </div>
      )}
    </div>
  );
};
