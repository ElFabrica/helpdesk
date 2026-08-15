import type { TicketPriority, TicketStatus } from "@/features/tickets/types";

export type DashboardIndicatorsResponseDTO = {
  total: number;
  byStatus: Partial<Record<TicketStatus, number>>;
  byPriority: Partial<Record<TicketPriority, number>>;
};

export type HighPriorityAlertDTO = {
  ticketId: number;
  title: string;
  priority: TicketPriority;
};

export type DashboardEvent =
  | {
      type: "indicators-updated";
      data: DashboardIndicatorsResponseDTO;
    }
  | {
      type: "high-priority-alert";
      data: HighPriorityAlertDTO;
    };
