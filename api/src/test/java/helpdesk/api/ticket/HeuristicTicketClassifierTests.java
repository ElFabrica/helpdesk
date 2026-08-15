package helpdesk.api.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import helpdesk.api.ticket.entity.ClassificationOrigin;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.service.ClassificationResult;
import helpdesk.api.ticket.service.HeuristicTicketClassifier;
import org.junit.jupiter.api.Test;

class HeuristicTicketClassifierTests {

    private final HeuristicTicketClassifier classifier = new HeuristicTicketClassifier();

    @Test
    void classifiesAccessIssue() {
        ClassificationResult classification = classifier.classify(
                "Problema de login",
                "Nao consigo acessar o sistema"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.ACESSO);
        assertThat(classification.priority()).isEqualTo(TicketPriority.MEDIA);
        assertThat(classification.origin()).isEqualTo(ClassificationOrigin.IA);
    }

    @Test
    void classifiesNetworkIssue() {
        ClassificationResult classification = classifier.classify(
                "Internet instavel",
                "A rede wifi cai durante o expediente"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.REDE);
        assertThat(classification.priority()).isEqualTo(TicketPriority.MEDIA);
        assertThat(classification.origin()).isEqualTo(ClassificationOrigin.IA);
    }

    @Test
    void classifiesHardwareIssue() {
        ClassificationResult classification = classifier.classify(
                "Impressora travada",
                "O teclado do computador tambem falha"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.HARDWARE);
        assertThat(classification.priority()).isEqualTo(TicketPriority.MEDIA);
        assertThat(classification.origin()).isEqualTo(ClassificationOrigin.IA);
    }

    @Test
    void classifiesSoftwareIssue() {
        ClassificationResult classification = classifier.classify(
                "Erro na pagina",
                "O sistema exibe bug ao salvar"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.SOFTWARE);
        assertThat(classification.priority()).isEqualTo(TicketPriority.MEDIA);
        assertThat(classification.origin()).isEqualTo(ClassificationOrigin.IA);
    }

    @Test
    void classifiesCriticalIssueAsHighPriority() {
        ClassificationResult classification = classifier.classify(
                "Sistema financeiro fora do ar",
                "Servico indisponivel desde cedo"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.SOFTWARE);
        assertThat(classification.priority()).isEqualTo(TicketPriority.ALTA);
        assertThat(classification.origin()).isEqualTo(ClassificationOrigin.IA);
    }

    @Test
    void classifiesLowCriticalityIssueAsLowPriority() {
        ClassificationResult classification = classifier.classify(
                "Duvida sobre cadastro",
                "Ajuste de baixa criticidade"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.OUTROS);
        assertThat(classification.priority()).isEqualTo(TicketPriority.BAIXA);
        assertThat(classification.origin()).isEqualTo(ClassificationOrigin.IA);
    }

    @Test
    void fallsBackToOtherAndMediumPriority() {
        ClassificationResult classification = classifier.classify(
                "Solicitacao interna",
                "Preciso de acompanhamento do atendimento"
        );

        assertThat(classification.category()).isEqualTo(TicketCategory.OUTROS);
        assertThat(classification.priority()).isEqualTo(TicketPriority.MEDIA);
        assertThat(classification.origin()).isEqualTo(ClassificationOrigin.IA);
    }
}
