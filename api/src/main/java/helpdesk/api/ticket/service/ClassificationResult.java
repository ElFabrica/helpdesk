package helpdesk.api.ticket.service;

import helpdesk.api.ticket.entity.ClassificationOrigin;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;

public record ClassificationResult(
        TicketCategory category,
        TicketPriority priority,
        ClassificationOrigin origin
) {
}
