import React from "react";
import { Clock, User, CheckCircle, AlertCircle } from "lucide-react";
import { ActivityEvent } from "@appTypes/api";
import { formatDate } from "@lib/utils";

interface ActivityTimelineProps {
  activities: ActivityEvent[];
  isLoading?: boolean;
}

const getActionIcon = (action: string) => {
  switch (action) {
    case "CREATED":
      return <CheckCircle className="h-4 w-4 text-green-500" />;
    case "ACKNOWLEDGED":
      return <CheckCircle className="h-4 w-4 text-blue-500" />;
    case "REVIEWED":
      return <CheckCircle className="h-4 w-4 text-purple-500" />;
    case "ASSIGNED":
      return <User className="h-4 w-4 text-indigo-500" />;
    case "APPROVED":
      return <CheckCircle className="h-4 w-4 text-green-600" />;
    case "REJECTED":
      return <AlertCircle className="h-4 w-4 text-red-500" />;
    default:
      return <Clock className="h-4 w-4 text-gray-500" />;
  }
};

export const ActivityTimeline: React.FC<ActivityTimelineProps> = ({
  activities,
  isLoading,
}) => {
  if (isLoading) {
    return (
      <div className="space-y-3">
        {[...Array(3)].map((_, i) => (
          <div key={i} className="h-16 bg-muted rounded animate-pulse" />
        ))}
      </div>
    );
  }

  if (!activities || activities.length === 0) {
    return (
      <div className="text-center py-6 text-muted-foreground">
        No activity recorded
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {activities.map((activity, index) => (
        <div key={activity.id} className="flex gap-4">
          <div className="flex flex-col items-center">
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-muted">
              {getActionIcon(activity.action)}
            </div>
            {index !== activities.length - 1 && (
              <div className="h-12 w-0.5 bg-border my-2" />
            )}
          </div>
          <div className="flex-1 pt-1">
            <div className="flex items-start justify-between">
              <div>
                <p className="font-semibold text-sm">{activity.action}</p>
                <p className="text-xs text-muted-foreground">
                  by {activity.performer}
                </p>
              </div>
              <span className="text-xs text-muted-foreground">
                {formatDate(activity.timestamp)}
              </span>
            </div>
            {activity.details && (
              <p className="text-sm text-foreground mt-1">{activity.details}</p>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};
