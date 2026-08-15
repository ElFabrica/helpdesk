package helpdesk.api.dashboard.dto;

import helpdesk.api.ticket.entity.TicketPriority;

public record HighPriorityAlertDTO(
        Long ticketId,
        String title,
        TicketPriority priority
) {
}
