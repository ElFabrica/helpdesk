import {
  ticketCategories,
  ticketPriorities,
  ticketStatuses,
  type TicketCategory,
  type TicketPriority,
  type TicketStatus,
} from "@/features/tickets/types";

export const statusLabels: Record<TicketStatus, string> = {
  ABERTO: "Aberto",
  EM_ANDAMENTO: "Em andamento",
  RESOLVIDO: "Resolvido",
  FECHADO: "Fechado",
};

export const priorityLabels: Record<TicketPriority, string> = {
  BAIXA: "Baixa",
  MEDIA: "Media",
  ALTA: "Alta",
};

export const categoryLabels: Record<TicketCategory, string> = {
  HARDWARE: "Hardware",
  SOFTWARE: "Software",
  ACESSO: "Acesso",
  REDE: "Rede",
  OUTROS: "Outros",
};

export const statusOptions = ticketStatuses.map((value) => ({
  value,
  label: statusLabels[value],
}));

export const priorityOptions = ticketPriorities.map((value) => ({
  value,
  label: priorityLabels[value],
}));

export const categoryOptions = ticketCategories.map((value) => ({
  value,
  label: categoryLabels[value],
}));
