import React from "react";
import { Link, useLocation } from "react-router-dom";
import {
  LayoutDashboard,
  AlertTriangle,
  BarChart3,
  Lightbulb,
  MapPin,
  Globe2,
  X,
} from "lucide-react";
import { useAuth } from "@context/auth";
import { cn } from "@lib/utils";

interface SidebarProps {
  open?: boolean;
  onClose?: () => void;
}

const navItems: Array<{
  href: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  roles: import("@appTypes/api").Role[];
}> = [
  {
    href: "/dashboard",
    label: "Dashboard",
    icon: LayoutDashboard,
    roles: ["ADMIN", "OPERATOR", "ANALYST", "VIEWER"],
  },
  {
    href: "/incidents",
    label: "Incidents",
    icon: AlertTriangle,
    roles: ["ADMIN", "OPERATOR"],
  },
  {
    href: "/analytics",
    label: "Analytics",
    icon: BarChart3,
    roles: ["ADMIN", "OPERATOR", "ANALYST"],
  },
  {
    href: "/spatial",
    label: "Spatial",
    icon: Globe2,
    roles: ["ADMIN", "OPERATOR", "ANALYST", "VIEWER"],
  },
  {
    href: "/recommendations",
    label: "Recommendations",
    icon: Lightbulb,
    roles: ["ADMIN", "OPERATOR", "ANALYST"],
  },
  {
    href: "/districts",
    label: "Districts",
    icon: MapPin,
    roles: ["ADMIN", "OPERATOR"],
  },
];

export const Sidebar: React.FC<SidebarProps> = ({ open = false, onClose }) => {
  const location = useLocation();
  const { user, hasRole } = useAuth();

  const visibleItems = navItems.filter((item) => hasRole(item.roles));

  return (
    <>
      {/* Mobile backdrop */}
      {open && (
        <div
          className="fixed inset-0 z-30 bg-black/50 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          "fixed left-0 top-16 z-40 h-[calc(100vh-64px)] w-64 border-r border-border bg-card transition-transform lg:relative lg:top-0 lg:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full",
        )}
      >
        <div className="flex flex-col h-full">
          {/* Close button for mobile */}
          <div className="flex items-center justify-between p-4 lg:hidden">
            <h2 className="font-semibold">Navigation</h2>
            <button
              onClick={onClose}
              className="inline-flex items-center justify-center rounded-md p-2 hover:bg-accent"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Nav Items */}
          <nav className="flex-1 space-y-1 p-4">
            {visibleItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.href;

              return (
                <Link
                  key={item.href}
                  to={item.href}
                  onClick={onClose}
                  className={cn(
                    "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                    isActive
                      ? "bg-primary text-primary-foreground"
                      : "text-foreground hover:bg-accent hover:text-accent-foreground",
                  )}
                >
                  <Icon className="h-5 w-5" />
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>

          {/* User Info */}
          <div className="border-t border-border p-4">
            <div className="text-xs text-muted-foreground">
              <p className="font-medium text-foreground mb-1">
                {user?.displayName}
              </p>
              <p>{user?.email}</p>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
};
