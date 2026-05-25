import React from "react";
import { IncidentTable } from "@components/common/IncidentTable";
import { Incident } from "@types/api";

// Mock data
const mockIncidents: Incident[] = Array.from({ length: 30 }, (_, i) => ({
  id: i + 1,
  districtId: (i % 5) + 1,
  title: `Incident ${i + 1}`,
  description: "Sample incident description",
  severity: (["LOW", "MEDIUM", "HIGH", "CRITICAL"] as const)[i % 4],
  status: (["OPEN", "IN_PROGRESS", "RESOLVED"] as const)[i % 3],
  createdAt: new Date(Date.now() - Math.random() * 86400000 * 30).toISOString(),
  updatedAt: new Date().toISOString(),
}));

export const IncidentsPage: React.FC = () => {
  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Incidents</h1>
        <p className="text-muted-foreground mt-2">
          View and manage all incidents across districts
        </p>
      </div>

      <IncidentTable incidents={mockIncidents} />
    </div>
  );
};
