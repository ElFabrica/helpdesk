export const ticketStatuses = ["ABERTO", "EM_ANDAMENTO", "RESOLVIDO", "FECHADO"] as const;
export const ticketPriorities = ["BAIXA", "MEDIA", "ALTA"] as const;
export const ticketCategories = ["HARDWARE", "SOFTWARE", "ACESSO", "REDE", "OUTROS"] as const;

export type TicketStatus = (typeof ticketStatuses)[number];
export type TicketPriority = (typeof ticketPriorities)[number];
export type TicketCategory = (typeof ticketCategories)[number];
export type ClassificationOrigin = "IA" | "MANUAL";

export type TicketSummaryResponseDTO = {
  id: number;
  title: string;
  category: TicketCategory;
  priority: TicketPriority;
  status: TicketStatus;
  requesterId: number;
  requesterName: string;
  responsibleId: number | null;
  createdAt: string;
};

export type TicketResponseDTO = TicketSummaryResponseDTO & {
  description: string;
  classificationOrigin: ClassificationOrigin;
  updatedAt: string;
};

export type TicketCommentResponseDTO = {
  id: number;
  authorId: number;
  authorName: string;
  text: string;
  createdAt: string;
};

export type TicketFilters = {
  status?: TicketStatus;
  priority?: TicketPriority;
  category?: TicketCategory;
};

export type CreateTicketRequestDTO = {
  title: string;
  description: string;
};

export type CreateTicketCommentRequestDTO = {
  text: string;
};

export type UpdateTicketRequestDTO = {
  title?: string;
  description?: string;
  status?: TicketStatus;
  priority?: TicketPriority;
  category?: TicketCategory;
};
