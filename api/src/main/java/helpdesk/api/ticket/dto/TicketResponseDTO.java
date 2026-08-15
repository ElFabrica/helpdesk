package helpdesk.api.ticket.dto;

import helpdesk.api.ticket.entity.ClassificationOrigin;
import helpdesk.api.ticket.entity.Ticket;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Detalhe completo de um chamado.")
public record TicketResponseDTO(
        @Schema(example = "42")
        Long id,

        @Schema(example = "Sistema financeiro fora do ar")
        String title,

        @Schema(example = "Nao consigo acessar o sistema financeiro desde cedo.")
        String description,

        @Schema(example = "SOFTWARE")
        TicketCategory category,

        @Schema(example = "ALTA")
        TicketPriority priority,

        @Schema(example = "ABERTO")
        TicketStatus status,

        @Schema(example = "IA")
        ClassificationOrigin classificationOrigin,

        @Schema(example = "2")
        Long requesterId,

        @Schema(example = "Maria Silva")
        String requesterName,

        @Schema(nullable = true, example = "1")
        Long responsibleId,

        @Schema(example = "2026-08-15T13:00:00Z")
        Instant createdAt,

        @Schema(example = "2026-08-15T13:10:00Z")
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
