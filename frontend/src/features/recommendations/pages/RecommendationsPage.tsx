import React, { useMemo, useState } from "react";
import { Card, CardContent } from "@components/ui/Card";
import { Badge } from "@components/ui/Badge";
import { Button } from "@components/ui/Button";
import { Input } from "@components/ui/Input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@components/ui/Dialog";
import { useRecommendations } from "@hooks/useRecommendations";
import {
  useApproveRecommendation,
  useRejectRecommendation,
  useActivityTimeline,
} from "@hooks/useWorkflow";
import { useRecommendationExplanations } from "@hooks/useAnalytics";
import { ActivityTimeline } from "@components/common/ActivityTimeline";
import { RecommendationDecisionDialog } from "@components/features/WorkflowDialogs";
import { PRIORITY_COLORS } from "@lib/utils";
import { Recommendation } from "@appTypes/api";

export const RecommendationsPage: React.FC = () => {
  const [search, setSearch] = useState("");
  const [selectedRec, setSelectedRec] = useState<Recommendation | null>(null);
  const [decisionDialogOpen, setDecisionDialogOpen] = useState(false);

  const { data, isLoading, error } = useRecommendations({
    page: 0,
    size: 20,
    sort: "priority,desc",
  });
  const { data: explanations } = useRecommendationExplanations();

  const { data: timeline, isLoading: timelineLoading } = useActivityTimeline(
    "RECOMMENDATION",
    selectedRec?.id ?? 0,
  );

  const approveMutation = useApproveRecommendation();
  const rejectMutation = useRejectRecommendation();

  const filteredRecommendations = useMemo(() => {
    const value = search.trim().toLowerCase();
    if (!value) return data?.content ?? [];

    return (data?.content ?? []).filter((item) => {
      return [item.type, item.message, item.districtName, item.priority]
        .filter((field): field is string => typeof field === "string")
        .some((field) => field.toLowerCase().includes(value));
    });
  }, [data?.content, search]);

  const explanationsByRecommendationId = useMemo(
    () =>
      new Map(
        (explanations ?? []).map((explanation) => [
          explanation.recommendationId,
          explanation,
        ]),
      ),
    [explanations],
  );

  const handleApprove = (decision: "APPROVED" | "REJECTED", reason: string) => {
    if (selectedRec) {
      const mutation =
        decision === "APPROVED" ? approveMutation : rejectMutation;
      mutation.mutate({
        recommendationId: selectedRec.id,
        request: { decision, reason },
      });
      setDecisionDialogOpen(false);
    }
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
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
                <Card
                  key={recommendation.id}
                  className="hover:border-primary/50"
                >
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
                        {explanationsByRecommendationId.has(
                          recommendation.id,
                        ) && (
                          <div className="mt-4 rounded-lg border border-border p-3">
                            <p className="text-sm font-medium">
                              Decision reasoning
                            </p>
                            <p className="mt-1 text-sm text-muted-foreground">
                              {
                                explanationsByRecommendationId.get(
                                  recommendation.id,
                                )?.reasoning
                              }
                            </p>
                            <div className="mt-3 flex flex-wrap gap-2 text-xs text-muted-foreground">
                              <span>
                                Impact:{" "}
                                {
                                  explanationsByRecommendationId.get(
                                    recommendation.id,
                                  )?.impact
                                }
                              </span>
                              <span>
                                Confidence:{" "}
                                {explanationsByRecommendationId
                                  .get(recommendation.id)
                                  ?.confidence.toFixed(0)}
                                %
                              </span>
                            </div>
                          </div>
                        )}
                        {recommendation.status && (
                          <Badge className="mt-2" variant="outline">
                            {recommendation.status}
                          </Badge>
                        )}
                      </div>

                      <div className="flex flex-col items-end gap-2">
                        <Badge
                          className={PRIORITY_COLORS[recommendation.priority]}
                        >
                          {recommendation.priority}
                        </Badge>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => setSelectedRec(recommendation)}
                        >
                          Details
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
        </div>
      )}

      {/* Detail Modal */}
      <Dialog
        open={!!selectedRec}
        onOpenChange={(open) => {
          if (!open) setSelectedRec(null);
        }}
      >
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{selectedRec?.type}</DialogTitle>
          </DialogHeader>
          <div className="space-y-6">
            <div>
              <h4 className="font-semibold mb-2">Details</h4>
              <p className="text-sm text-muted-foreground">
                {selectedRec?.message}
              </p>
              <div className="flex gap-4 mt-4">
                <div>
                  <p className="text-xs text-muted-foreground">Priority</p>
                  <Badge
                    className={
                      selectedRec ? PRIORITY_COLORS[selectedRec.priority] : ""
                    }
                  >
                    {selectedRec?.priority}
                  </Badge>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Status</p>
                  <Badge variant="outline">
                    {selectedRec?.status || "PENDING"}
                  </Badge>
                </div>
              </div>
            </div>

            {selectedRec &&
              explanationsByRecommendationId.has(selectedRec.id) && (
                <div>
                  <h4 className="font-semibold mb-2">Operational reasoning</h4>
                  <div className="rounded-lg border border-border p-4">
                    <p className="text-sm text-muted-foreground">
                      {
                        explanationsByRecommendationId.get(selectedRec.id)
                          ?.reasoning
                      }
                    </p>
                    <div className="mt-4 grid gap-3 sm:grid-cols-2">
                      {Object.entries(
                        explanationsByRecommendationId.get(selectedRec.id)
                          ?.evidence ?? {},
                      ).map(([key, value]) => (
                        <div key={key} className="rounded-md bg-muted p-3">
                          <p className="text-xs text-muted-foreground">{key}</p>
                          <p className="font-semibold">{String(value)}</p>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}

            <div>
              <h4 className="font-semibold mb-3">Activity Timeline</h4>
              <ActivityTimeline
                activities={timeline ?? []}
                isLoading={timelineLoading}
              />
            </div>

            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setSelectedRec(null)}>
                Close
              </Button>
              {selectedRec?.status === "PENDING" && (
                <Button onClick={() => setDecisionDialogOpen(true)}>
                  Make Decision
                </Button>
              )}
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <RecommendationDecisionDialog
        open={decisionDialogOpen}
        onOpenChange={setDecisionDialogOpen}
        onDecision={handleApprove}
        isLoading={approveMutation.isPending || rejectMutation.isPending}
      />
    </div>
  );
};
