package helpdesk.api.ticket.dto;

import helpdesk.api.ticket.entity.TicketComment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Comentario ou evento de historico de um chamado.")
public record TicketCommentResponseDTO(
        @Schema(example = "10")
        Long id,

        @Schema(example = "1")
        Long authorId,

        @Schema(example = "Maria Silva")
        String authorName,

        @Schema(example = "Estamos verificando o problema.")
        String text,

        @Schema(example = "2026-08-15T13:00:00Z")
        Instant createdAt
) {
    public static TicketCommentResponseDTO from(TicketComment comment) {
        return new TicketCommentResponseDTO(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getText(),
                comment.getCreatedAt()
        );
    }
}
