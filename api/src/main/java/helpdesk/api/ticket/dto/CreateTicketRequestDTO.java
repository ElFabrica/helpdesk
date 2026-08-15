package helpdesk.api.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para abertura de chamado.")
public record CreateTicketRequestDTO(
        @Schema(description = "Titulo curto do problema.", example = "Sistema financeiro fora do ar")
        @NotBlank String title,

        @Schema(description = "Descricao detalhada usada pela classificacao automatica.", example = "Nao consigo acessar o sistema financeiro desde cedo.")
        @NotBlank String description
) {
}
