import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { toast } from "sonner";
import {
  connectDashboardEvents,
  getDashboardIndicators,
} from "@/features/dashboard/dashboard-api";
import type {
  DashboardEvent,
  DashboardIndicatorsResponseDTO,
  HighPriorityAlertDTO,
} from "@/features/dashboard/types";
import type { TicketPriority, TicketStatus } from "@/features/tickets/types";

export type ConnectionStatus = "connecting" | "connected" | "reconnecting" | "offline";

const emptyIndicators: DashboardIndicatorsResponseDTO = {
  total: 0,
  byStatus: {},
  byPriority: {},
};

export function useDashboardData(isAdmin: boolean) {
  const [indicators, setIndicators] = useState<DashboardIndicatorsResponseDTO>(emptyIndicators);
  const [alerts, setAlerts] = useState<HighPriorityAlertDTO[]>([]);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>("connecting");
  const [lastUpdatedAt, setLastUpdatedAt] = useState<Date | null>(null);
  const reconnectAttemptRef = useRef(0);
  const reconnectTimeoutRef = useRef<number | null>(null);

  const loadIndicators = useCallback(async (showLoading = true) => {
    if (showLoading) {
      setIsLoading(true);
    }
    setError("");

    try {
      const loadedIndicators = await getDashboardIndicators();
      setIndicators(loadedIndicators);
      setLastUpdatedAt(new Date());
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Nao foi possivel carregar os indicadores.");
    } finally {
      if (showLoading) {
        setIsLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    if (!isAdmin) {
      setIsLoading(false);
      return;
    }

    void loadIndicators();
  }, [isAdmin, loadIndicators]);

  useEffect(() => {
    if (!isAdmin) {
      return undefined;
    }

    let isCurrent = true;
    let controller: AbortController | null = null;

    function clearReconnectTimer() {
      if (reconnectTimeoutRef.current) {
        window.clearTimeout(reconnectTimeoutRef.current);
        reconnectTimeoutRef.current = null;
      }
    }

    function handleEvent(event: DashboardEvent) {
      if (event.type === "indicators-updated") {
        setIndicators(event.data);
        setLastUpdatedAt(new Date());
        return;
      }

      setAlerts((currentAlerts) => [event.data, ...currentAlerts].slice(0, 5));
      toast.warning(`Chamado #${event.data.ticketId} com prioridade alta`, {
        description: event.data.title,
      });
    }

    function scheduleReconnect() {
      if (!isCurrent) {
        return;
      }

      void loadIndicators(false);
      setConnectionStatus("reconnecting");

      const delay = Math.min(1000 * 2 ** reconnectAttemptRef.current, 10000);
      reconnectAttemptRef.current += 1;
      reconnectTimeoutRef.current = window.setTimeout(connect, delay);
    }

    async function connect() {
      clearReconnectTimer();
      controller?.abort();
      controller = new AbortController();
      setConnectionStatus(reconnectAttemptRef.current ? "reconnecting" : "connecting");

      try {
        await connectDashboardEvents(
          controller.signal,
          handleEvent,
          () => {
            reconnectAttemptRef.current = 0;
            setConnectionStatus("connected");
          }
        );

        scheduleReconnect();
      } catch (streamError) {
        if (!isCurrent || controller.signal.aborted) {
          return;
        }

        setError(streamError instanceof Error ? streamError.message : "A conexao em tempo real caiu.");
        scheduleReconnect();
      }
    }

    void connect();

    return () => {
      isCurrent = false;
      clearReconnectTimer();
      controller?.abort();
      setConnectionStatus("offline");
    };
  }, [isAdmin, loadIndicators]);

  const lastUpdatedLabel = useMemo(() => {
    if (!lastUpdatedAt) {
      return "Aguardando dados";
    }

    return new Intl.DateTimeFormat("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    }).format(lastUpdatedAt);
  }, [lastUpdatedAt]);

  return {
    alerts,
    connectionStatus,
    error,
    indicators,
    isLoading,
    lastUpdatedLabel,
    loadIndicators,
  };
}

export function getStatusCount(indicators: DashboardIndicatorsResponseDTO, status: TicketStatus) {
  return indicators.byStatus[status] ?? 0;
}

export function getPriorityCount(indicators: DashboardIndicatorsResponseDTO, priority: TicketPriority) {
  return indicators.byPriority[priority] ?? 0;
}
