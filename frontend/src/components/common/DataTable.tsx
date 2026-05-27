import React from "react";
import { cn } from "@lib/utils";

export interface DataTableColumn<T> {
  header: string;
  accessor: keyof T | ((row: T) => React.ReactNode);
  width?: string;
  align?: "left" | "center" | "right";
}

interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  data: T[];
  isLoading?: boolean;
  emptyText?: string;
  rowKey?: keyof T;
  actions?: (row: T) => React.ReactNode;
}

export function DataTable<T extends object>({
  columns,
  data,
  isLoading = false,
  emptyText = "No records found",
  rowKey = "id" as keyof T,
  actions,
}: DataTableProps<T>) {
  if (isLoading) {
    return (
      <div className="rounded-lg border border-border bg-card p-6">
        <div className="space-y-3">
          {[...Array(5)].map((_, index) => (
            <div
              key={index}
              className="h-10 rounded-md bg-muted animate-pulse"
            />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-border bg-card">
      <table className="min-w-full text-sm">
        <thead className="bg-muted/50 text-left text-xs uppercase tracking-wide text-muted-foreground">
          <tr>
            {columns.map((column) => (
              <th
                key={column.header}
                className={cn(
                  "py-3 px-4 font-semibold",
                  column.align === "center" && "text-center",
                  column.align === "right" && "text-right",
                )}
                style={column.width ? { width: column.width } : undefined}
              >
                {column.header}
              </th>
            ))}
            {actions && (
              <th className="py-3 px-4 text-right font-semibold">Actions</th>
            )}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length + (actions ? 1 : 0)}
                className="py-12 px-4 text-center text-muted-foreground"
              >
                {emptyText}
              </td>
            </tr>
          ) : (
            data.map((row) => (
              <tr
                key={String(row[rowKey])}
                className="border-t border-border hover:bg-muted/40"
              >
                {columns.map((column) => {
                  const value: React.ReactNode =
                    typeof column.accessor === "function"
                      ? column.accessor(row)
                      : (row[column.accessor] as unknown as React.ReactNode);

                  return (
                    <td
                      key={String(column.header)}
                      className={cn(
                        "py-3 px-4 align-top",
                        column.align === "center" && "text-center",
                        column.align === "right" && "text-right",
                      )}
                    >
                      {value}
                    </td>
                  );
                })}
                {actions && (
                  <td className="py-3 px-4 text-right">{actions(row)}</td>
                )}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
