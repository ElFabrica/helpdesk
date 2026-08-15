package helpdesk.api.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criacao de comentario no chamado.")
public record CreateTicketCommentRequestDTO(
        @Schema(description = "Texto da interacao ou comentario.", example = "Estamos verificando o problema.")
        @NotBlank String text
) {
}
