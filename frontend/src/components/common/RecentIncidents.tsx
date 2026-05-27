import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import { Badge } from "@components/ui/Badge";
import { SEVERITY_COLORS } from "@lib/utils";

interface Incident {
  id: number;
  title?: string;
  type?: string;
  description?: string;
  districtId?: number;
  districtName?: string;
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  status: string;
  createdAt: string;
}

interface RecentIncidentsProps {
  incidents: Incident[];
  isLoading?: boolean;
}

export const RecentIncidents: React.FC<RecentIncidentsProps> = ({
  incidents,
  isLoading = false,
}) => {
  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Recent Incidents</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="h-12 bg-muted animate-pulse rounded" />
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Recent Incidents</CardTitle>
      </CardHeader>
      <CardContent>
        {incidents.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-muted-foreground">No incidents found</p>
          </div>
        ) : (
          <div className="space-y-4">
            {incidents.map((incident) => (
              <div
                key={incident.id}
                className="flex items-start gap-4 pb-4 border-b border-border last:border-0"
              >
                <div className="flex-1 min-w-0">
                  <h4 className="font-medium text-sm truncate">
                    {incident.title || incident.type || incident.description}
                  </h4>
                  <p className="text-xs text-muted-foreground mt-1">
                    {incident.districtName
                      ? `${incident.districtName} • `
                      : incident.districtId
                        ? `District ${incident.districtId} • `
                        : ""}
                    {new Date(incident.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <Badge className={SEVERITY_COLORS[incident.severity]}>
                  {incident.severity}
                </Badge>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
};
