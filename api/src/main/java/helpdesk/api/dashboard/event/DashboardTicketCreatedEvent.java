package helpdesk.api.dashboard.event;

import helpdesk.api.ticket.dto.TicketSummaryResponseDTO;

public record DashboardTicketCreatedEvent(TicketSummaryResponseDTO ticket) {
}
