package helpdesk.api.dashboard.dto;

import helpdesk.api.ticket.entity.TicketPriority;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Alerta emitido quando chamado de prioridade alta e aberto.")
public record HighPriorityAlertDTO(
        @Schema(example = "42")
        Long ticketId,

        @Schema(example = "Sistema financeiro fora do ar")
        String title,

        @Schema(example = "ALTA")
        TicketPriority priority
) {
}
