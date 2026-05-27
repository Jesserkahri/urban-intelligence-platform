import React, { useState } from "react";
import { Badge } from "@components/ui/Badge";
import { Button } from "@components/ui/Button";
import { Input } from "@components/ui/Input";
import { Card, CardContent, CardHeader, CardTitle } from "@components/ui/Card";
import { DataTable, DataTableColumn } from "@components/common/DataTable";
import { FormField } from "@components/common/FormField";
import {
  Incident,
  IncidentCreateRequest,
  IncidentStatus,
  IncidentSeverity,
} from "@appTypes/api";
import { IncidentQueryParams } from "@services/incident";
import {
  useCreateIncident,
  useDeleteIncident,
  useIncidents,
  useRecentIncidents,
  useUpdateIncident,
} from "@hooks/useIncidents";
import { useDistricts } from "@hooks/useDistricts";
import { formatDate, SEVERITY_COLORS, STATUS_COLORS } from "@lib/utils";

const severityOptions: IncidentSeverity[] = [
  "LOW",
  "MEDIUM",
  "HIGH",
  "CRITICAL",
];

const statusOptions: IncidentStatus[] = [
  "OPEN",
  "IN_PROGRESS",
  "RESOLVED",
  "CLOSED",
];

const sortOptions = [
  { label: "Newest", value: "createdAt,desc" },
  { label: "Oldest", value: "createdAt,asc" },
  { label: "Severity", value: "severity,desc" },
  { label: "Status", value: "status,asc" },
];

const defaultFormState = {
  type: "",
  description: "",
  severity: "MEDIUM" as IncidentSeverity,
  latitude: 0,
  longitude: 0,
  districtId: 0,
  status: "OPEN" as IncidentStatus,
};

export const IncidentsPage: React.FC = () => {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<IncidentStatus | "">("");
  const [districtFilter, setDistrictFilter] = useState<number | "">("");
  const [sort, setSort] = useState("createdAt,desc");
  const [page, setPage] = useState(0);
  const [form, setForm] = useState<
    IncidentCreateRequest & { status?: IncidentStatus }
  >(defaultFormState);
  const [selectedIncidentId, setSelectedIncidentId] = useState<number | null>(
    null,
  );
  const [formError, setFormError] = useState<string | null>(null);

  const incidentQueryParams: IncidentQueryParams = {
    page,
    size: 10,
    sort,
    status: statusFilter || undefined,
    districtId: districtFilter === "" ? undefined : Number(districtFilter),
    search: search || undefined,
  };

  const {
    data: incidentPage,
    isLoading: isIncidentsLoading,
    error: incidentsError,
    isFetching: isIncidentsFetching,
  } = useIncidents(incidentQueryParams);

  const { data: districtList } = useDistricts({
    page: 0,
    size: 100,
    sort: "name,asc",
  });

  const { data: recentIncidents, isLoading: isRecentLoading } =
    useRecentIncidents();

  const createMutation = useCreateIncident();
  const updateMutation = useUpdateIncident();
  const deleteMutation = useDeleteIncident();

  const incidentRows = incidentPage?.content ?? [];
  const districtOptions = districtList?.content ?? [];

  const totalPages = incidentPage?.totalPages ?? 1;

  const columns: DataTableColumn<Incident>[] = [
    { header: "Type", accessor: "type" },
    {
      header: "Severity",
      accessor: (incident) => (
        <Badge className={SEVERITY_COLORS[incident.severity]}>
          {incident.severity}
        </Badge>
      ),
    },
    {
      header: "Status",
      accessor: (incident) => (
        <Badge className={STATUS_COLORS[incident.status]}>
          {incident.status}
        </Badge>
      ),
    },
    {
      header: "District",
      accessor: (incident) => incident.districtName ?? `${incident.districtId}`,
    },
    {
      header: "Created",
      accessor: (incident) => formatDate(incident.createdAt),
    },
  ];

  const handleSelectIncident = (incident: Incident) => {
    setSelectedIncidentId(incident.id);
    setForm({
      type: incident.type,
      description: incident.description,
      severity: incident.severity,
      latitude: incident.latitude,
      longitude: incident.longitude,
      districtId: incident.districtId,
      status: incident.status,
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const resetForm = () => {
    setSelectedIncidentId(null);
    setForm(defaultFormState);
    setFormError(null);
  };

  const validateForm = () => {
    if (!form.type.trim()) return "Incident type is required.";
    if (!form.description.trim()) return "Description is required.";
    if (!form.severity) return "Severity is required.";
    if (form.latitude === null || form.latitude === undefined)
      return "Latitude is required.";
    if (form.longitude === null || form.longitude === undefined)
      return "Longitude is required.";
    if (!form.districtId) return "District is required.";
    return null;
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    const error = validateForm();
    if (error) {
      setFormError(error);
      return;
    }

    setFormError(null);
    try {
      if (selectedIncidentId) {
        await updateMutation.mutateAsync({
          id: selectedIncidentId,
          payload: {
            type: form.type,
            description: form.description,
            severity: form.severity,
            latitude: Number(form.latitude),
            longitude: Number(form.longitude),
            status: form.status,
          },
        });
      } else {
        await createMutation.mutateAsync({
          type: form.type,
          description: form.description,
          severity: form.severity,
          latitude: Number(form.latitude),
          longitude: Number(form.longitude),
          districtId: Number(form.districtId),
        });
      }
      resetForm();
    } catch (error) {
      setFormError(
        "Unable to save incident. Please review your input and try again.",
      );
    }
  };

  const handleStatusToggle = async (incident: Incident) => {
    const nextStatus: IncidentStatus =
      incident.status === "OPEN"
        ? "IN_PROGRESS"
        : incident.status === "IN_PROGRESS"
          ? "RESOLVED"
          : incident.status;

    if (nextStatus === incident.status) return;
    await updateMutation.mutateAsync({
      id: incident.id,
      payload: { status: nextStatus },
    });
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("Delete this incident? This action cannot be undone."))
      return;
    await deleteMutation.mutateAsync(id);
  };

  const actionButtons = (incident: Incident) => (
    <div className="flex flex-wrap justify-end gap-2">
      <Button
        size="sm"
        variant="outline"
        onClick={() => handleSelectIncident(incident)}
      >
        Edit
      </Button>
      <Button
        size="sm"
        variant="secondary"
        onClick={() => handleStatusToggle(incident)}
        disabled={
          incident.status === "RESOLVED" || incident.status === "CLOSED"
        }
      >
        {incident.status === "OPEN" ? "Start" : "Advance"}
      </Button>
      <Button
        size="sm"
        variant="destructive"
        onClick={() => handleDelete(incident.id)}
        disabled={deleteMutation.isPending}
      >
        Delete
      </Button>
    </div>
  );

  const incidentCount = incidentPage?.totalElements ?? 0;

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Incidents</h1>
        <p className="text-muted-foreground mt-2">
          View and manage all incidents across districts.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>
            {selectedIncidentId ? "Edit Incident" : "Create Incident"}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={handleSubmit}
            className="grid gap-6 lg:grid-cols-[1.5fr_1fr]"
          >
            <div className="space-y-4">
              <FormField
                label="Type"
                htmlFor="incident-type"
                error={formError?.includes("type") ? formError : undefined}
              >
                <Input
                  id="incident-type"
                  value={form.type}
                  onChange={(event) =>
                    setForm({ ...form, type: event.target.value })
                  }
                  placeholder="Traffic congestion, air quality, etc."
                />
              </FormField>

              <FormField
                label="Description"
                htmlFor="incident-description"
                error={
                  formError?.includes("Description") ? formError : undefined
                }
              >
                <Input
                  id="incident-description"
                  value={form.description}
                  onChange={(event) =>
                    setForm({ ...form, description: event.target.value })
                  }
                  placeholder="Describe the incident"
                />
              </FormField>

              <div className="grid gap-4 sm:grid-cols-2">
                <FormField label="Severity" htmlFor="incident-severity">
                  <select
                    id="incident-severity"
                    value={form.severity}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        severity: event.target.value as IncidentSeverity,
                      })
                    }
                    className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm outline-none"
                  >
                    {severityOptions.map((severity) => (
                      <option key={severity} value={severity}>
                        {severity}
                      </option>
                    ))}
                  </select>
                </FormField>

                <FormField label="Status" htmlFor="incident-status">
                  <select
                    id="incident-status"
                    value={form.status}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        status: event.target.value as IncidentStatus,
                      })
                    }
                    className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm outline-none"
                  >
                    {statusOptions.map((statusOption) => (
                      <option key={statusOption} value={statusOption}>
                        {statusOption}
                      </option>
                    ))}
                  </select>
                </FormField>
              </div>
            </div>

            <div className="space-y-4">
              <FormField
                label="District"
                htmlFor="incident-district"
                error={formError?.includes("District") ? formError : undefined}
              >
                <select
                  id="incident-district"
                  value={form.districtId || ""}
                  onChange={(event) =>
                    setForm({ ...form, districtId: Number(event.target.value) })
                  }
                  className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm outline-none"
                >
                  <option value="">Select a district</option>
                  {districtOptions.map((district) => (
                    <option key={district.id} value={district.id}>
                      {district.name}
                    </option>
                  ))}
                </select>
              </FormField>

              <div className="grid gap-4 sm:grid-cols-2">
                <FormField label="Latitude" htmlFor="incident-latitude">
                  <Input
                    id="incident-latitude"
                    type="number"
                    step="0.0001"
                    value={form.latitude}
                    onChange={(event) =>
                      setForm({ ...form, latitude: Number(event.target.value) })
                    }
                    placeholder="Latitude"
                  />
                </FormField>
                <FormField label="Longitude" htmlFor="incident-longitude">
                  <Input
                    id="incident-longitude"
                    type="number"
                    step="0.0001"
                    value={form.longitude}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        longitude: Number(event.target.value),
                      })
                    }
                    placeholder="Longitude"
                  />
                </FormField>
              </div>

              {formError && (
                <div className="rounded-md border border-destructive bg-destructive/10 p-3 text-sm text-destructive">
                  {formError}
                </div>
              )}

              <div className="flex flex-wrap gap-3 pt-2">
                <Button
                  type="submit"
                  disabled={
                    createMutation.isPending || updateMutation.isPending
                  }
                >
                  {selectedIncidentId ? "Save changes" : "Create incident"}
                </Button>
                {selectedIncidentId && (
                  <Button type="button" variant="outline" onClick={resetForm}>
                    Cancel edit
                  </Button>
                )}
              </div>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between w-full">
            <div>
              <CardTitle>Incident ledger</CardTitle>
              <p className="text-sm text-muted-foreground">
                {incidentCount} incidents matched your filters.
              </p>
            </div>
            <div className="grid w-full max-w-4xl gap-3 sm:grid-cols-4">
              <Input
                placeholder="Search incidents"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
              <select
                value={statusFilter}
                onChange={(event) => {
                  setStatusFilter(event.target.value as IncidentStatus | "");
                  setPage(0);
                }}
                className="rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="">All statuses</option>
                {statusOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
              <select
                value={districtFilter}
                onChange={(event) => {
                  setDistrictFilter(
                    event.target.value === "" ? "" : Number(event.target.value),
                  );
                  setPage(0);
                }}
                className="rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="">All districts</option>
                {districtOptions.map((district) => (
                  <option key={district.id} value={district.id}>
                    {district.name}
                  </option>
                ))}
              </select>
              <select
                value={sort}
                onChange={(event) => {
                  setSort(event.target.value);
                  setPage(0);
                }}
                className="rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                {sortOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </CardHeader>

        <CardContent>
          {incidentsError ? (
            <div className="rounded-md border border-destructive bg-destructive/10 p-4 text-sm text-destructive">
              Failed to load incidents. Refresh or check connectivity.
            </div>
          ) : (
            <DataTable
              columns={columns}
              data={incidentRows}
              isLoading={isIncidentsLoading || isIncidentsFetching}
              actions={actionButtons}
              emptyText="No incidents matched the current filters"
            />
          )}

          <div className="mt-4 flex items-center justify-between gap-3 text-sm text-muted-foreground">
            <p>
              Page {incidentPage?.number != null ? incidentPage.number + 1 : 1}{" "}
              of {totalPages}
            </p>
            <div className="flex gap-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
              >
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Recent incidents</CardTitle>
        </CardHeader>
        <CardContent>
          {isRecentLoading ? (
            <div className="space-y-3">
              {[...Array(4)].map((_, index) => (
                <div
                  key={index}
                  className="h-10 rounded-md bg-muted animate-pulse"
                />
              ))}
            </div>
          ) : (
            <ul className="space-y-3">
              {recentIncidents?.slice(0, 5).map((incident) => (
                <li
                  key={incident.id}
                  className="rounded-lg border border-border p-4"
                >
                  <div className="flex items-center justify-between gap-4">
                    <div>
                      <p className="font-semibold">{incident.type}</p>
                      <p className="text-sm text-muted-foreground">
                        {incident.districtName ??
                          `District ${incident.districtId}`}{" "}
                        • {formatDate(incident.createdAt)}
                      </p>
                    </div>
                    <Badge className={SEVERITY_COLORS[incident.severity]}>
                      {incident.severity}
                    </Badge>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
