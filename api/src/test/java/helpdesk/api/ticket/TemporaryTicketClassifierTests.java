package helpdesk.api.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TemporaryTicketClassifierTests {

    private final TemporaryTicketClassifier classifier = new TemporaryTicketClassifier();

    @Test
    void classifiesAccessIssue() {
        TicketClassification classification = classifier.classify(
                "Problema de login",
                "Nao consigo acessar o sistema"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.ACESSO);
        assertThat(classification.priority()).isEqualTo(TicketPriority.MEDIA);
        assertThat(classification.origin()).isEqualTo(ClassificationOrigin.IA);
    }

    @Test
    void classifiesCriticalIssueAsHighPriority() {
        TicketClassification classification = classifier.classify(
                "Sistema financeiro fora do ar",
                "Servico indisponivel desde cedo"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.SOFTWARE);
        assertThat(classification.priority()).isEqualTo(TicketPriority.ALTA);
    }

    @Test
    void fallsBackToOtherAndMediumPriority() {
        TicketClassification classification = classifier.classify(
                "Solicitacao interna",
                "Preciso de acompanhamento do atendimento"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.OUTROS);
        assertThat(classification.priority()).isEqualTo(TicketPriority.MEDIA);
    }
}
