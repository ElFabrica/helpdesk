package helpdesk.api.ticket.dto;

import helpdesk.api.ticket.entity.Ticket;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import java.time.Instant;

public record TicketSummaryResponseDTO(
        Long id,
        String title,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        Long requesterId,
        String requesterName,
        Long responsibleId,
        Instant createdAt
) {
    public static TicketSummaryResponseDTO from(Ticket ticket) {
        Long responsibleId = ticket.getResponsible() == null ? null : ticket.getResponsible().getId();

        return new TicketSummaryResponseDTO(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getRequester().getId(),
                ticket.getRequester().getName(),
                responsibleId,
                ticket.getCreatedAt()
        );
    }
}
