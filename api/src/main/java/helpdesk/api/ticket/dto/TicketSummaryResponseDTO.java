package helpdesk.api.ticket.dto;

import helpdesk.api.ticket.entity.Ticket;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Resumo de chamado usado em listagens.")
public record TicketSummaryResponseDTO(
        @Schema(example = "42")
        Long id,

        @Schema(example = "Sistema financeiro fora do ar")
        String title,

        @Schema(example = "SOFTWARE")
        TicketCategory category,

        @Schema(example = "ALTA")
        TicketPriority priority,

        @Schema(example = "ABERTO")
        TicketStatus status,

        @Schema(example = "2")
        Long requesterId,

        @Schema(example = "Maria Silva")
        String requesterName,

        @Schema(nullable = true, example = "1")
        Long responsibleId,

        @Schema(example = "2026-08-15T13:00:00Z")
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
