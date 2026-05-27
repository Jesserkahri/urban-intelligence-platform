import React, { useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import { Badge } from "@components/ui/Badge";
import { Button } from "@components/ui/Button";
import { Input } from "@components/ui/Input";
import { useRecommendations } from "@hooks/useRecommendations";
import { PRIORITY_COLORS } from "@lib/utils";
import { Recommendation, RecommendationPriority } from "@appTypes/api";

const priorityOptions: RecommendationPriority[] = ["LOW", "MEDIUM", "HIGH"];

export const RecommendationsPage: React.FC = () => {
  const [search, setSearch] = useState("");
  const { data, isLoading, error } = useRecommendations({
    page: 0,
    size: 20,
    sort: "priority,desc",
  });

  const filteredRecommendations = useMemo(() => {
    const value = search.trim().toLowerCase();
    if (!value) return data?.content ?? [];

    return (data?.content ?? []).filter((item) => {
      return [item.type, item.message, item.districtName, item.priority]
        .filter(Boolean)
        .some((field) => field.toLowerCase().includes(value));
    });
  }, [data?.content, search]);

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Recommendations</h1>
        <p className="text-muted-foreground mt-2">
          Operational recommendations from the analytics engine.
        </p>
      </div>

      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <Input
          placeholder="Search recommendations"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          className="max-w-md"
        />
        <div className="text-sm text-muted-foreground">
          Showing {filteredRecommendations.length} of {data?.totalElements ?? 0}
        </div>
      </div>

      {error ? (
        <div className="rounded-lg border border-destructive bg-destructive/10 p-6 text-sm text-destructive">
          Unable to load recommendations. Please try again.
        </div>
      ) : (
        <div className="grid gap-4">
          {isLoading
            ? [...Array(4)].map((_, index) => (
                <Card key={index}>
                  <CardContent>
                    <div className="space-y-3">
                      <div className="h-4 w-1/2 rounded bg-muted animate-pulse" />
                      <div className="h-3 w-full rounded bg-muted animate-pulse" />
                      <div className="h-3 w-5/6 rounded bg-muted animate-pulse" />
                    </div>
                  </CardContent>
                </Card>
              ))
            : filteredRecommendations.map((recommendation: Recommendation) => (
                <Card key={recommendation.id}>
                  <CardContent className="pt-6">
                    <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                      <div className="flex-1">
                        <h3 className="font-semibold text-lg">
                          {recommendation.type}
                        </h3>
                        <p className="text-sm text-muted-foreground mt-1">
                          {recommendation.message}
                        </p>
                        <p className="text-xs text-muted-foreground mt-2">
                          District: {recommendation.districtName}
                        </p>
                      </div>

                      <div className="flex items-center gap-2">
                        <Badge
                          className={PRIORITY_COLORS[recommendation.priority]}
                        >
                          {recommendation.priority}
                        </Badge>
                        <Button size="sm" variant="outline">
                          Review
                        </Button>
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
