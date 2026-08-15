import { api, getApiErrorMessage, getApiUrl, getStoredToken } from "@/lib/api";
import type {
  DashboardEvent,
  DashboardIndicatorsResponseDTO,
  HighPriorityAlertDTO,
} from "@/features/dashboard/types";

type DashboardEventHandler = (event: DashboardEvent) => void;
type DashboardConnectionHandler = () => void;

type SseMessage = {
  event: string;
  data: string;
};

export async function getDashboardIndicators() {
  try {
    const response = await api.get<DashboardIndicatorsResponseDTO>("/dashboard/indicators");
    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error, "Nao foi possivel carregar os indicadores."));
  }
}

export async function connectDashboardEvents(
  signal: AbortSignal,
  onEvent: DashboardEventHandler,
  onOpen?: DashboardConnectionHandler
) {
  const token = getStoredToken();
  if (!token) {
    throw new Error("Autenticacao necessaria para conectar ao dashboard.");
  }

  const response = await fetch(getApiUrl("/dashboard/events"), {
    headers: {
      Accept: "text/event-stream",
      Authorization: `Bearer ${token}`,
    },
    signal,
  });

  if (!response.ok) {
    throw new Error("Nao foi possivel conectar aos eventos do dashboard.");
  }

  if (!response.body) {
    throw new Error("O navegador nao disponibilizou a stream de eventos.");
  }

  onOpen?.();

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  while (!signal.aborted) {
    const { value, done } = await reader.read();

    if (done) {
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    const messages = buffer.split(/\r?\n\r?\n/);
    buffer = messages.pop() ?? "";

    for (const message of messages) {
      const parsedMessage = parseSseMessage(message);
      if (parsedMessage) {
        dispatchDashboardEvent(parsedMessage, onEvent);
      }
    }
  }
}

function parseSseMessage(message: string): SseMessage | null {
  const lines = message.split(/\r?\n/);
  let event = "message";
  const dataLines: string[] = [];

  for (const line of lines) {
    if (!line || line.startsWith(":")) {
      continue;
    }

    if (line.startsWith("event:")) {
      event = line.slice("event:".length).trim();
      continue;
    }

    if (line.startsWith("data:")) {
      dataLines.push(line.slice("data:".length).trimStart());
    }
  }

  if (!dataLines.length) {
    return null;
  }

  return {
    event,
    data: dataLines.join("\n"),
  };
}

function dispatchDashboardEvent(message: SseMessage, onEvent: DashboardEventHandler) {
  if (message.event === "indicators-updated") {
    onEvent({
      type: "indicators-updated",
      data: JSON.parse(message.data) as DashboardIndicatorsResponseDTO,
    });
    return;
  }

  if (message.event === "high-priority-alert") {
    onEvent({
      type: "high-priority-alert",
      data: JSON.parse(message.data) as HighPriorityAlertDTO,
    });
  }
}
