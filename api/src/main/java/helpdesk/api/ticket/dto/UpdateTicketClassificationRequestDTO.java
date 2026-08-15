package helpdesk.api.ticket.dto;

import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketClassificationRequestDTO(
        @NotNull
        TicketCategory category,

        @NotNull
        TicketPriority priority
) {
}
