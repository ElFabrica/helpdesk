package helpdesk.api.ticket.dto;

import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Correcao manual de categoria e prioridade.")
public record UpdateTicketClassificationRequestDTO(
        @Schema(description = "Nova categoria do chamado.", example = "REDE")
        @NotNull
        TicketCategory category,

        @Schema(description = "Nova prioridade do chamado.", example = "MEDIA")
        @NotNull
        TicketPriority priority
) {
}
