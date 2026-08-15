package helpdesk.api.ticket.dto;

import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Campos opcionais para atualizacao de chamado.")
public record UpdateTicketRequestDTO(
        @Schema(description = "Novo titulo.", example = "Sistema financeiro com instabilidade")
        String title,

        @Schema(description = "Nova descricao.", example = "O sistema voltou parcialmente, mas ainda apresenta erro.")
        String description,

        @Schema(description = "Novo status.", example = "EM_ANDAMENTO")
        TicketStatus status,

        @Schema(description = "Nova prioridade. Campo administrativo.", example = "MEDIA")
        TicketPriority priority,

        @Schema(description = "Nova categoria. Campo administrativo.", example = "SOFTWARE")
        TicketCategory category,

        @Schema(description = "ID do responsavel. Campo administrativo.", nullable = true, example = "1")
        Long responsibleId
) {
    public boolean hasAdminFields() {
        return priority != null || category != null || responsibleId != null;
    }
}
