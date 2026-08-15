package helpdesk.api.ticket.service;

import helpdesk.api.ticket.entity.ClassificationOrigin;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class HeuristicTicketClassifier implements TicketClassifier {

    @Override
    public ClassificationResult classify(String title, String description) {
        String text = normalize(title) + " " + normalize(description);

        return new ClassificationResult(
                categoryFor(text),
                priorityFor(text),
                ClassificationOrigin.IA
        );
    }

    private TicketCategory categoryFor(String text) {
        if (containsAny(text, "senha", "login", "acesso")) {
            return TicketCategory.ACESSO;
        }
        if (containsAny(text, "internet", "rede", "wifi")) {
            return TicketCategory.REDE;
        }
        if (containsAny(text, "computador", "impressora", "teclado")) {
            return TicketCategory.HARDWARE;
        }
        if (containsAny(text, "sistema", "erro", "bug", "pagina")) {
            return TicketCategory.SOFTWARE;
        }

        return TicketCategory.OUTROS;
    }

    private TicketPriority priorityFor(String text) {
        if (containsAny(text, "fora do ar", "indisponivel", "urgente", "parada")) {
            return TicketPriority.ALTA;
        }
        if (containsAny(text, "duvida", "ajuste", "baixa criticidade")) {
            return TicketPriority.BAIXA;
        }

        return TicketPriority.MEDIA;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String lowerCase = value.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lowerCase, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
