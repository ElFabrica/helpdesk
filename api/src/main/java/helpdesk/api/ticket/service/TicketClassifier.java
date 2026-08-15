package helpdesk.api.ticket.service;

public interface TicketClassifier {
    ClassificationResult classify(String title, String description);
}
