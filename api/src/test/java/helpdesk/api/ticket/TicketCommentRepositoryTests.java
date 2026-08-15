package helpdesk.api.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import helpdesk.api.user.User;
import helpdesk.api.user.UserRepository;
import helpdesk.api.user.UserRole;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TicketCommentRepositoryTests {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsTicketCommentsOrderedByCreatedAt() {
        User requester = userRepository.save(new User(
                "Solicitante",
                "requester-comments@example.com",
                "hash",
                UserRole.SOLICITANTE
        ));
        User author = userRepository.save(new User(
                "Atendente",
                "author-comments@example.com",
                "hash",
                UserRole.ADMIN
        ));
        Ticket ticket = ticketRepository.save(new Ticket(
                "Problema de acesso",
                "Nao consigo acessar o sistema.",
                TicketCategory.ACESSO,
                TicketPriority.MEDIA,
                ClassificationOrigin.MANUAL,
                requester,
                null
        ));

        TicketComment firstComment = ticketCommentRepository.save(new TicketComment(
                ticket,
                author,
                "Estamos verificando o problema."
        ));
        TicketComment secondComment = ticketCommentRepository.save(new TicketComment(
                ticket,
                requester,
                "Aguardando retorno."
        ));

        List<TicketComment> comments = ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());

        assertThat(comments).containsExactly(firstComment, secondComment);
        assertThat(comments).allSatisfy(comment -> assertThat(comment.getTicket().getId()).isEqualTo(ticket.getId()));
    }

    @Test
    void mapsTicketCommentsRelationship() {
        User requester = userRepository.save(new User(
                "Solicitante Relacionamento",
                "requester-relationship@example.com",
                "hash",
                UserRole.SOLICITANTE
        ));
        Ticket ticket = ticketRepository.save(new Ticket(
                "Impressora indisponivel",
                "A impressora do setor nao responde.",
                TicketCategory.HARDWARE,
                TicketPriority.BAIXA,
                ClassificationOrigin.MANUAL,
                requester,
                null
        ));
        ticketCommentRepository.save(new TicketComment(
                ticket,
                requester,
                "Chamado aberto para acompanhamento."
        ));
        ticketRepository.flush();
        ticketCommentRepository.flush();
        entityManager.clear();

        Ticket persistedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();

        assertThat(persistedTicket.getComments())
                .hasSize(1)
                .first()
                .extracting(TicketComment::getText)
                .isEqualTo("Chamado aberto para acompanhamento.");
    }
}
