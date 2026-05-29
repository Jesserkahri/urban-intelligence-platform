import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  assignIncident,
  acknowledgeIncident,
  reviewIncident,
  approveRecommendation,
  rejectRecommendation,
  getActivityTimeline,
} from "@services/workflow";
import {
  IncidentAssignmentRequest,
  IncidentReviewRequest,
  RecommendationDecisionRequest,
} from "@appTypes/api";

// Incident workflow hooks
export const useAssignIncident = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      incidentId,
      request,
    }: {
      incidentId: number;
      request: IncidentAssignmentRequest;
    }) => assignIncident(incidentId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["incidents"] });
      queryClient.invalidateQueries({ queryKey: ["activities"] });
    },
  });
};

export const useAcknowledgeIncident = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      incidentId,
      notes,
    }: {
      incidentId: number;
      notes?: string;
    }) => acknowledgeIncident(incidentId, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["incidents"] });
      queryClient.invalidateQueries({ queryKey: ["activities"] });
    },
  });
};

export const useReviewIncident = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      incidentId,
      request,
    }: {
      incidentId: number;
      request: IncidentReviewRequest;
    }) => reviewIncident(incidentId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["incidents"] });
      queryClient.invalidateQueries({ queryKey: ["activities"] });
    },
  });
};

// Recommendation workflow hooks
export const useApproveRecommendation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      recommendationId,
      request,
    }: {
      recommendationId: number;
      request: RecommendationDecisionRequest;
    }) => approveRecommendation(recommendationId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["recommendations"] });
      queryClient.invalidateQueries({ queryKey: ["activities"] });
    },
  });
};

export const useRejectRecommendation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      recommendationId,
      request,
    }: {
      recommendationId: number;
      request: RecommendationDecisionRequest;
    }) => rejectRecommendation(recommendationId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["recommendations"] });
      queryClient.invalidateQueries({ queryKey: ["activities"] });
    },
  });
};

// Activity timeline hook
export const useActivityTimeline = (entityType: string, entityId: number) => {
  return useQuery({
    queryKey: ["activities", entityType, entityId],
    queryFn: () => getActivityTimeline(entityType, entityId),
    enabled: !!entityId,
  });
};
