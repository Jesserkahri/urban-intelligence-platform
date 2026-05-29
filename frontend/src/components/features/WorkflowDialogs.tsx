import React, { useState } from "react";
import { Button } from "@components/ui/Button";
import { Input } from "@components/ui/Input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@components/ui/Dialog";

interface AssignIncidentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onAssign: (assignedTo: string, notes?: string) => void;
  isLoading?: boolean;
}

export const AssignIncidentDialog: React.FC<AssignIncidentDialogProps> = ({
  open,
  onOpenChange,
  onAssign,
  isLoading,
}) => {
  const [assignedTo, setAssignedTo] = useState("");
  const [notes, setNotes] = useState("");

  const handleSubmit = () => {
    if (assignedTo.trim()) {
      onAssign(assignedTo, notes);
      setAssignedTo("");
      setNotes("");
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Assign Incident</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium">Assign to</label>
            <Input
              placeholder="Name or ID"
              value={assignedTo}
              onChange={(e) => setAssignedTo(e.target.value)}
              disabled={isLoading}
            />
          </div>
          <div>
            <label className="text-sm font-medium">Notes (optional)</label>
            <textarea
              placeholder="Assignment notes..."
              className="w-full min-h-24 px-3 py-2 border rounded-md text-sm"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              disabled={isLoading}
            />
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={isLoading || !assignedTo.trim()}
          >
            Assign
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

interface ReviewIncidentDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onReview: (
    status: "ACKNOWLEDGED" | "REVIEWED" | "REJECTED",
    notes: string,
  ) => void;
  isLoading?: boolean;
}

export const ReviewIncidentDialog: React.FC<ReviewIncidentDialogProps> = ({
  open,
  onOpenChange,
  onReview,
  isLoading,
}) => {
  const [status, setStatus] = useState<
    "ACKNOWLEDGED" | "REVIEWED" | "REJECTED"
  >("ACKNOWLEDGED");
  const [notes, setNotes] = useState("");

  const handleSubmit = () => {
    if (notes.trim()) {
      onReview(status, notes);
      setNotes("");
      setStatus("ACKNOWLEDGED");
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Review Incident</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium">Status</label>
            <select
              className="w-full px-3 py-2 border rounded-md text-sm"
              value={status}
              onChange={(e) => setStatus(e.target.value as any)}
              disabled={isLoading}
            >
              <option value="ACKNOWLEDGED">Acknowledged</option>
              <option value="REVIEWED">Reviewed</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>
          <div>
            <label className="text-sm font-medium">Review Notes</label>
            <textarea
              placeholder="Enter your review notes..."
              className="w-full min-h-24 px-3 py-2 border rounded-md text-sm"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              disabled={isLoading}
            />
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={isLoading || !notes.trim()}>
            Submit Review
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

interface RecommendationDecisionDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onDecision: (decision: "APPROVED" | "REJECTED", reason: string) => void;
  isLoading?: boolean;
}

export const RecommendationDecisionDialog: React.FC<
  RecommendationDecisionDialogProps
> = ({ open, onOpenChange, onDecision, isLoading }) => {
  const [decision, setDecision] = useState<"APPROVED" | "REJECTED">("APPROVED");
  const [reason, setReason] = useState("");

  const handleSubmit = () => {
    if (reason.trim()) {
      onDecision(decision, reason);
      setReason("");
      setDecision("APPROVED");
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Make Decision on Recommendation</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium">Decision</label>
            <select
              className="w-full px-3 py-2 border rounded-md text-sm"
              value={decision}
              onChange={(e) => setDecision(e.target.value as any)}
              disabled={isLoading}
            >
              <option value="APPROVED">Approve</option>
              <option value="REJECTED">Reject</option>
            </select>
          </div>
          <div>
            <label className="text-sm font-medium">Reason</label>
            <textarea
              placeholder="Explain your decision..."
              className="w-full min-h-24 px-3 py-2 border rounded-md text-sm"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              disabled={isLoading}
            />
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={isLoading || !reason.trim()}>
            Submit Decision
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
