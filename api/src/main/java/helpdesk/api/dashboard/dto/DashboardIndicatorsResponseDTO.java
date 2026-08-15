package helpdesk.api.dashboard.dto;

import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Indicadores agregados do dashboard.")
public record DashboardIndicatorsResponseDTO(
        @Schema(description = "Total de chamados.", example = "12")
        long total,

        @Schema(description = "Quantidade de chamados por status.")
        Map<TicketStatus, Long> byStatus,

        @Schema(description = "Quantidade de chamados por prioridade.")
        Map<TicketPriority, Long> byPriority
) {
}
