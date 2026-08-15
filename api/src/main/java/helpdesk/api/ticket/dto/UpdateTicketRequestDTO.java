package helpdesk.api.ticket.dto;

import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;

public record UpdateTicketRequestDTO(
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        TicketCategory category,
        Long responsibleId
) {
    public boolean hasAdminFields() {
        return priority != null || category != null || responsibleId != null;
    }
}
