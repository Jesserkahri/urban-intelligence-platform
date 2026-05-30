import React, {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@context/auth";
import {
  LiveDashboardSnapshot,
  LiveOperationEvent,
  OperationalNotification,
} from "@appTypes/api";
import { buildOperationsStreamUrl } from "@services/operations";

interface LiveOperationsContextType {
  connected: boolean;
  latestEvent: LiveOperationEvent | null;
  events: LiveOperationEvent[];
  snapshot: LiveDashboardSnapshot | null;
  latestNotification: OperationalNotification | null;
}

const LiveOperationsContext = createContext<
  LiveOperationsContextType | undefined
>(undefined);

const EVENT_TYPES = [
  "STREAM_CONNECTED",
  "INCIDENT_CREATED",
  "INCIDENT_UPDATED",
  "INCIDENT_STATUS_CHANGED",
  "INCIDENT_DELETED",
  "CRITICAL_INCIDENT",
  "ESCALATION",
  "NOTIFICATION_CREATED",
  "NOTIFICATION_ACKNOWLEDGED",
  "DASHBOARD_SNAPSHOT",
];

export const LiveOperationsProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const { isAuthenticated } = useAuth();
  const queryClient = useQueryClient();
  const [connected, setConnected] = useState(false);
  const [events, setEvents] = useState<LiveOperationEvent[]>([]);
  const [snapshot, setSnapshot] = useState<LiveDashboardSnapshot | null>(null);
  const [latestNotification, setLatestNotification] =
    useState<OperationalNotification | null>(null);

  useEffect(() => {
    if (!isAuthenticated) return;
    const streamUrl = buildOperationsStreamUrl("all");
    if (!streamUrl) return;

    const source = new EventSource(streamUrl);

    const handleEvent = (message: MessageEvent<string>) => {
      let event: LiveOperationEvent;
      try {
        event = JSON.parse(message.data) as LiveOperationEvent;
      } catch (error) {
        console.error("Unable to parse live operations event", error);
        return;
      }

      setConnected(true);
      setEvents((current) => [event, ...current].slice(0, 25));

      if (event.payload?.snapshot) {
        setSnapshot(event.payload.snapshot);
        queryClient.setQueryData(
          ["operations", "dashboard"],
          event.payload.snapshot,
        );
      }

      if (event.payload?.notification) {
        setLatestNotification(event.payload.notification);
      }

      if (event.channel === "incidents") {
        queryClient.invalidateQueries({ queryKey: ["incidents"] });
        queryClient.invalidateQueries({ queryKey: ["analytics", "hotspots"] });
        queryClient.invalidateQueries({ queryKey: ["analytics", "daily"] });
      }

      if (event.channel === "alerts" || event.channel === "notifications") {
        queryClient.invalidateQueries({ queryKey: ["operations"] });
      }

      if (event.channel === "dashboard") {
        queryClient.invalidateQueries({ queryKey: ["analytics", "dashboard"] });
      }
    };

    EVENT_TYPES.forEach((eventType) =>
      source.addEventListener(eventType, handleEvent as EventListener),
    );

    source.onopen = () => setConnected(true);
    source.onerror = () => setConnected(false);

    return () => {
      EVENT_TYPES.forEach((eventType) =>
        source.removeEventListener(eventType, handleEvent as EventListener),
      );
      source.close();
      setConnected(false);
    };
  }, [isAuthenticated, queryClient]);

  const value = useMemo<LiveOperationsContextType>(
    () => ({
      connected,
      latestEvent: events[0] ?? null,
      events,
      snapshot,
      latestNotification,
    }),
    [connected, events, latestNotification, snapshot],
  );

  return (
    <LiveOperationsContext.Provider value={value}>
      {children}
    </LiveOperationsContext.Provider>
  );
};

export const useLiveOperations = () => {
  const context = useContext(LiveOperationsContext);
  if (!context) {
    throw new Error(
      "useLiveOperations must be used within LiveOperationsProvider",
    );
  }
  return context;
};
