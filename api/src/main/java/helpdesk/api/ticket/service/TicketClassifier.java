package helpdesk.api.ticket.service;

public interface TicketClassifier {
    TicketClassification classify(String title, String description);
}
