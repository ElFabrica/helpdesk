package helpdesk.api.ticket;

import java.time.Instant;

public record TicketResponseDTO(
        Long id,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        ClassificationOrigin classificationOrigin,
        Long requesterId,
        String requesterName,
        Long responsibleId,
        Instant createdAt,
        Instant updatedAt
) {
    public static TicketResponseDTO from(Ticket ticket) {
        Long responsibleId = ticket.getResponsible() == null ? null : ticket.getResponsible().getId();

        return new TicketResponseDTO(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getClassificationOrigin(),
                ticket.getRequester().getId(),
                ticket.getRequester().getName(),
                responsibleId,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
