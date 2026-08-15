package helpdesk.api.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequestDTO(
        @NotBlank String title,
        @NotBlank String description
) {
}
