package helpdesk.api.ticket;

public record TicketClassification(
        TicketCategory category,
        TicketPriority priority,
        ClassificationOrigin origin
) {
}
