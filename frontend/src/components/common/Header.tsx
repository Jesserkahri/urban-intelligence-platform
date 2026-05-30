import React, { useState } from "react";
import { Bell, Check, LogOut, Menu, Radio } from "lucide-react";
import { useAuth } from "@context/auth";
import { Button } from "@components/ui/Button";
import { useLiveOperations } from "@context/liveOperations";
import {
  useAcknowledgeNotification,
  useNotifications,
} from "@hooks/useOperations";
import { formatDate } from "@lib/utils";

interface HeaderProps {
  onMenuClick: () => void;
}

export const Header: React.FC<HeaderProps> = ({ onMenuClick }) => {
  const { user, logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const { connected, latestEvent, snapshot } = useLiveOperations();
  const { data: notifications } = useNotifications({
    size: 8,
    unreadOnly: false,
  });
  const acknowledgeMutation = useAcknowledgeNotification();

  const unreadCount =
    snapshot?.unreadNotifications ??
    notifications?.content.filter((notification) => !notification.acknowledged)
      .length ??
    0;

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-card">
      <div className="flex items-center justify-between px-4 py-4 sm:px-6">
        <div className="flex items-center gap-4">
          <button
            onClick={onMenuClick}
            className="lg:hidden inline-flex items-center justify-center rounded-md p-2 hover:bg-accent"
          >
            <Menu className="h-6 w-6" />
          </button>
          <h1 className="text-xl font-bold text-foreground hidden sm:block">
            Urban Flagship
          </h1>
        </div>

        <div className="flex items-center gap-4">
          <div className="hidden items-center gap-2 rounded-md border border-border px-3 py-2 text-xs text-muted-foreground md:flex">
            <Radio
              className={`h-4 w-4 ${connected ? "text-green-500" : "text-muted-foreground"}`}
            />
            <span>{connected ? "Live" : "Reconnecting"}</span>
            {latestEvent && (
              <span className="max-w-[220px] truncate">
                {latestEvent.title}
              </span>
            )}
          </div>

          <div className="relative">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setNotificationsOpen((value) => !value)}
              title="Notifications"
            >
              <Bell className="h-5 w-5" />
              {unreadCount > 0 && (
                <span className="absolute -right-1 -top-1 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-destructive px-1 text-[10px] font-semibold text-destructive-foreground">
                  {unreadCount > 9 ? "9+" : unreadCount}
                </span>
              )}
            </Button>

            {notificationsOpen && (
              <div className="absolute right-0 mt-3 w-[min(380px,calc(100vw-2rem))] rounded-lg border border-border bg-card shadow-lg">
                <div className="flex items-center justify-between border-b border-border px-4 py-3">
                  <div>
                    <p className="text-sm font-semibold">Notifications</p>
                    <p className="text-xs text-muted-foreground">
                      {connected ? "Live operations stream" : "Stream offline"}
                    </p>
                  </div>
                </div>
                <div className="max-h-96 overflow-auto p-2">
                  {(notifications?.content ?? []).length === 0 ? (
                    <p className="px-3 py-6 text-center text-sm text-muted-foreground">
                      No operational alerts yet.
                    </p>
                  ) : (
                    notifications?.content.map((notification) => (
                      <div
                        key={notification.id}
                        className="rounded-md border border-border p-3"
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <p className="text-sm font-medium">
                              {notification.title}
                            </p>
                            <p className="mt-1 text-xs text-muted-foreground">
                              {notification.message}
                            </p>
                            <p className="mt-2 text-[11px] text-muted-foreground">
                              {notification.severity} -{" "}
                              {formatDate(notification.createdAt)}
                            </p>
                          </div>
                          {!notification.acknowledged && (
                            <Button
                              size="icon"
                              variant="ghost"
                              title="Acknowledge"
                              onClick={() =>
                                acknowledgeMutation.mutate(notification.id)
                              }
                            >
                              <Check className="h-4 w-4" />
                            </Button>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>

          <div className="flex items-center gap-2">
            <div className="text-right text-sm hidden sm:block">
              <p className="font-medium text-foreground">{user?.displayName}</p>
              <p className="text-xs text-muted-foreground">{user?.role}</p>
            </div>
          </div>

          <Button
            variant="ghost"
            size="icon"
            onClick={handleLogout}
            disabled={isLoggingOut}
            title="Logout"
          >
            <LogOut className="h-5 w-5" />
          </Button>
        </div>
      </div>
    </header>
  );
};

