package helpdesk.api.ticket;

public interface TicketClassifier {
    TicketClassification classify(String title, String description);
}
