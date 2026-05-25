import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatDate(date: string | Date): string {
  const d = typeof date === "string" ? new Date(date) : date;
  return d.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

export function formatCurrency(value: number): string {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(value);
}

export function formatPercent(value: number): string {
  return `${Math.round(value * 100) / 100}%`;
}

export const SEVERITY_COLORS = {
  LOW: "bg-blue-50 text-blue-900 dark:bg-blue-900 dark:text-blue-50",
  MEDIUM: "bg-yellow-50 text-yellow-900 dark:bg-yellow-900 dark:text-yellow-50",
  HIGH: "bg-orange-50 text-orange-900 dark:bg-orange-900 dark:text-orange-50",
  CRITICAL: "bg-red-50 text-red-900 dark:bg-red-900 dark:text-red-50",
};

export const STATUS_COLORS = {
  OPEN: "bg-gray-50 text-gray-900 dark:bg-gray-900 dark:text-gray-50",
  IN_PROGRESS: "bg-blue-50 text-blue-900 dark:bg-blue-900 dark:text-blue-50",
  RESOLVED: "bg-green-50 text-green-900 dark:bg-green-900 dark:text-green-50",
  CLOSED: "bg-gray-100 text-gray-900 dark:bg-gray-800 dark:text-gray-50",
};

export const PRIORITY_COLORS = {
  LOW: "bg-green-50 text-green-900 dark:bg-green-900 dark:text-green-50",
  MEDIUM: "bg-yellow-50 text-yellow-900 dark:bg-yellow-900 dark:text-yellow-50",
  HIGH: "bg-red-50 text-red-900 dark:bg-red-900 dark:text-red-50",
};
