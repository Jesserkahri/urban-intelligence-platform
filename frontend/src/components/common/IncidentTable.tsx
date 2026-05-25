import React, { useState } from "react";
import { Incident } from "@types/api";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import { Badge } from "@components/ui/Badge";
import { SEVERITY_COLORS, STATUS_COLORS } from "@lib/utils";

interface IncidentTableProps {
  incidents: Incident[];
  isLoading?: boolean;
}

export const IncidentTable: React.FC<IncidentTableProps> = ({
  incidents,
  isLoading = false,
}) => {
  const [page, setPage] = useState(1);
  const itemsPerPage = 10;
  const paginatedIncidents = incidents.slice(
    (page - 1) * itemsPerPage,
    page * itemsPerPage,
  );
  const totalPages = Math.ceil(incidents.length / itemsPerPage);

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Incidents</CardTitle>
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
        <CardTitle>Incidents</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left py-3 px-4 font-medium">Title</th>
                <th className="text-left py-3 px-4 font-medium">Severity</th>
                <th className="text-left py-3 px-4 font-medium">Status</th>
                <th className="text-left py-3 px-4 font-medium">Created</th>
              </tr>
            </thead>
            <tbody>
              {paginatedIncidents.length === 0 ? (
                <tr>
                  <td
                    colSpan={4}
                    className="text-center py-8 text-muted-foreground"
                  >
                    No incidents found
                  </td>
                </tr>
              ) : (
                paginatedIncidents.map((incident) => (
                  <tr
                    key={incident.id}
                    className="border-b border-border hover:bg-muted/50"
                  >
                    <td className="py-3 px-4">{incident.title}</td>
                    <td className="py-3 px-4">
                      <Badge className={SEVERITY_COLORS[incident.severity]}>
                        {incident.severity}
                      </Badge>
                    </td>
                    <td className="py-3 px-4">
                      <Badge
                        className={
                          STATUS_COLORS[
                            incident.status as keyof typeof STATUS_COLORS
                          ]
                        }
                      >
                        {incident.status}
                      </Badge>
                    </td>
                    <td className="py-3 px-4 text-muted-foreground">
                      {new Date(incident.createdAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between mt-4 pt-4 border-t border-border">
              <p className="text-sm text-muted-foreground">
                Page {page} of {totalPages}
              </p>
              <div className="flex gap-2">
                <button
                  onClick={() => setPage(Math.max(1, page - 1))}
                  disabled={page === 1}
                  className="px-3 py-1 rounded text-sm border border-border hover:bg-accent disabled:opacity-50"
                >
                  Previous
                </button>
                <button
                  onClick={() => setPage(Math.min(totalPages, page + 1))}
                  disabled={page === totalPages}
                  className="px-3 py-1 rounded text-sm border border-border hover:bg-accent disabled:opacity-50"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
};
