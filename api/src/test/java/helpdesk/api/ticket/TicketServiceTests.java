package helpdesk.api.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import helpdesk.api.auth.service.JwtTokenService;
import helpdesk.api.ticket.dto.CreateTicketRequestDTO;
import helpdesk.api.ticket.dto.TicketResponseDTO;
import helpdesk.api.ticket.dto.TicketSummaryResponseDTO;
import helpdesk.api.ticket.dto.UpdateTicketRequestDTO;
import helpdesk.api.ticket.entity.ClassificationOrigin;
import helpdesk.api.ticket.entity.Ticket;
import helpdesk.api.ticket.entity.TicketComment;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import helpdesk.api.ticket.repository.TicketCommentRepository;
import helpdesk.api.ticket.repository.TicketRepository;
import helpdesk.api.ticket.service.TicketService;
import helpdesk.api.user.entity.User;
import helpdesk.api.user.entity.UserRole;
import helpdesk.api.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class TicketServiceTests {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsTicketForAuthenticatedUser() {
        User requester = userRepository.save(new User(
                "Maria Solicitante",
                "maria-ticket-service@example.com",
                "hash",
                UserRole.SOLICITANTE
        ));
        authenticateAs(requester);

        TicketResponseDTO response = ticketService.create(new CreateTicketRequestDTO(
                "Sistema financeiro fora do ar",
                "Nao consigo acessar o sistema financeiro desde cedo."
        ));

        Ticket savedTicket = ticketRepository.findById(response.id()).orElseThrow();

        assertThat(response.status()).isEqualTo(TicketStatus.ABERTO);
        assertThat(response.requesterId()).isEqualTo(requester.getId());
        assertThat(response.category()).isEqualTo(TicketCategory.SOFTWARE);
        assertThat(response.priority()).isEqualTo(TicketPriority.ALTA);
        assertThat(response.classificationOrigin()).isEqualTo(ClassificationOrigin.IA);
        assertThat(savedTicket.getRequester().getId()).isEqualTo(requester.getId());
        assertThat(savedTicket.getResponsible()).isNull();
    }

    @Test
    void adminListsAllTicketsWithCombinedFiltersByNewestFirst() {
        User admin = saveUser("Ana Admin", "ana-ticket-list-service@example.com", UserRole.ADMIN);
        User maria = saveUser("Maria Solicitante", "maria-ticket-list-service@example.com", UserRole.SOLICITANTE);
        User joao = saveUser("Joao Solicitante", "joao-ticket-list-service@example.com", UserRole.SOLICITANTE);
        Ticket olderMatchingTicket = saveTicket(
                "Sistema lento",
                TicketCategory.SOFTWARE,
                TicketPriority.ALTA,
                maria
        );
        saveTicket("Troca de monitor", TicketCategory.HARDWARE, TicketPriority.ALTA, maria);
        Ticket newerMatchingTicket = saveTicket(
                "Aplicacao indisponivel",
                TicketCategory.SOFTWARE,
                TicketPriority.ALTA,
                joao
        );
        authenticateAs(admin);

        List<TicketSummaryResponseDTO> tickets = ticketService.list(
                TicketStatus.ABERTO,
                TicketPriority.ALTA,
                TicketCategory.SOFTWARE
        );

        assertThat(tickets)
                .extracting(TicketSummaryResponseDTO::id)
                .containsExactly(newerMatchingTicket.getId(), olderMatchingTicket.getId());
    }

    @Test
    void requesterListsOnlyOwnTickets() {
        User maria = saveUser("Maria Solicitante", "maria-own-ticket-list-service@example.com", UserRole.SOLICITANTE);
        User joao = saveUser("Joao Solicitante", "joao-own-ticket-list-service@example.com", UserRole.SOLICITANTE);
        Ticket ownTicket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, maria);
        saveTicket("Acesso ao VPN", TicketCategory.ACESSO, TicketPriority.MEDIA, joao);
        authenticateAs(maria);

        List<TicketSummaryResponseDTO> tickets = ticketService.list(null, null, null);

        assertThat(tickets)
                .extracting(TicketSummaryResponseDTO::id)
                .containsExactly(ownTicket.getId());
    }

    @Test
    void returnsEmptyListWhenNoTicketMatchesFilters() {
        User requester = saveUser("Maria Solicitante", "maria-empty-ticket-list-service@example.com", UserRole.SOLICITANTE);
        saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);
        authenticateAs(requester);

        List<TicketSummaryResponseDTO> tickets = ticketService.list(null, TicketPriority.BAIXA, null);

        assertThat(tickets).isEmpty();
    }

    @Test
    void requesterGetsOwnTicketDetail() {
        User requester = saveUser("Maria Solicitante", "maria-detail-ticket-service@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);
        authenticateAs(requester);

        TicketResponseDTO response = ticketService.detail(ticket.getId());

        assertThat(response.id()).isEqualTo(ticket.getId());
        assertThat(response.description()).isEqualTo("Descricao do chamado");
        assertThat(response.requesterId()).isEqualTo(requester.getId());
    }

    @Test
    void requesterCannotGetAnotherRequesterTicketDetail() {
        User maria = saveUser("Maria Solicitante", "maria-forbidden-detail-service@example.com", UserRole.SOLICITANTE);
        User joao = saveUser("Joao Solicitante", "joao-forbidden-detail-service@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, joao);
        authenticateAs(maria);

        assertThatThrownBy(() -> ticketService.detail(ticket.getId()))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void adminUpdatesTicketClassificationAndResponsible() {
        User admin = saveUser("Ana Admin", "ana-update-ticket-service@example.com", UserRole.ADMIN);
        User requester = saveUser("Maria Solicitante", "maria-admin-update-ticket-service@example.com", UserRole.SOLICITANTE);
        User responsible = saveUser("Bruno Admin", "bruno-admin-update-ticket-service@example.com", UserRole.ADMIN);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);
        authenticateAs(admin);

        TicketResponseDTO response = ticketService.update(ticket.getId(), new UpdateTicketRequestDTO(
                null,
                null,
                TicketStatus.EM_ANDAMENTO,
                TicketPriority.MEDIA,
                TicketCategory.REDE,
                responsible.getId()
        ));

        assertThat(response.status()).isEqualTo(TicketStatus.EM_ANDAMENTO);
        assertThat(response.priority()).isEqualTo(TicketPriority.MEDIA);
        assertThat(response.category()).isEqualTo(TicketCategory.REDE);
        assertThat(response.responsibleId()).isEqualTo(responsible.getId());
    }

    @Test
    void requesterUpdatesOwnTicketTextAndStatus() {
        User requester = saveUser("Maria Solicitante", "maria-update-own-ticket-service@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);
        authenticateAs(requester);

        TicketResponseDTO response = ticketService.update(ticket.getId(), new UpdateTicketRequestDTO(
                "Sistema indisponivel",
                "Nao consigo acessar o sistema desde cedo.",
                TicketStatus.RESOLVIDO,
                null,
                null,
                null
        ));

        assertThat(response.title()).isEqualTo("Sistema indisponivel");
        assertThat(response.description()).isEqualTo("Nao consigo acessar o sistema desde cedo.");
        assertThat(response.status()).isEqualTo(TicketStatus.RESOLVIDO);
    }

    @Test
    void statusChangeCreatesHistoryEvent() {
        User requester = saveUser("Maria Solicitante", "maria-status-history-service@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);
        authenticateAs(requester);

        ticketService.update(ticket.getId(), new UpdateTicketRequestDTO(
                null,
                null,
                TicketStatus.EM_ANDAMENTO,
                null,
                null,
                null
        ));

        List<TicketComment> comments = ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());
        assertThat(comments)
                .hasSize(1)
                .first()
                .satisfies(comment -> {
                    assertThat(comment.getAuthor().getId()).isEqualTo(requester.getId());
                    assertThat(comment.getText()).isEqualTo("Status alterado de ABERTO para EM_ANDAMENTO");
                });
    }

    @ParameterizedTest
    @CsvSource({
            "ABERTO, EM_ANDAMENTO",
            "ABERTO, RESOLVIDO",
            "ABERTO, FECHADO",
            "EM_ANDAMENTO, RESOLVIDO",
            "EM_ANDAMENTO, FECHADO",
            "RESOLVIDO, FECHADO"
    })
    void acceptsAllowedStatusTransitions(TicketStatus currentStatus, TicketStatus newStatus) {
        User requester = saveUser(
                "Maria Solicitante",
                "maria-allowed-transition-" + currentStatus + "-" + newStatus + "@example.com",
                UserRole.SOLICITANTE
        );
        Ticket ticket = saveTicketWithStatus(
                "Sistema lento",
                TicketCategory.SOFTWARE,
                TicketPriority.ALTA,
                requester,
                currentStatus
        );
        authenticateAs(requester);

        TicketResponseDTO response = ticketService.update(ticket.getId(), new UpdateTicketRequestDTO(
                null,
                null,
                newStatus,
                null,
                null,
                null
        ));

        assertThat(response.status()).isEqualTo(newStatus);
        assertThat(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()))
                .extracting(TicketComment::getText)
                .containsExactly("Status alterado de " + currentStatus + " para " + newStatus);
    }

    @ParameterizedTest
    @CsvSource({
            "EM_ANDAMENTO, ABERTO",
            "RESOLVIDO, ABERTO",
            "RESOLVIDO, EM_ANDAMENTO",
            "FECHADO, ABERTO",
            "FECHADO, EM_ANDAMENTO",
            "FECHADO, RESOLVIDO"
    })
    void rejectsForbiddenStatusTransitions(TicketStatus currentStatus, TicketStatus newStatus) {
        User requester = saveUser(
                "Maria Solicitante",
                "maria-forbidden-transition-" + currentStatus + "-" + newStatus + "@example.com",
                UserRole.SOLICITANTE
        );
        Ticket ticket = saveTicketWithStatus(
                "Sistema lento",
                TicketCategory.SOFTWARE,
                TicketPriority.ALTA,
                requester,
                currentStatus
        );
        authenticateAs(requester);

        assertThatThrownBy(() -> ticketService.update(ticket.getId(), new UpdateTicketRequestDTO(
                null,
                null,
                newStatus,
                null,
                null,
                null
        )))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        Ticket unchangedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(unchangedTicket.getStatus()).isEqualTo(currentStatus);
        assertThat(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId())).isEmpty();
    }

    @Test
    void requesterCannotUpdateAdminTicketFields() {
        User requester = saveUser("Maria Solicitante", "maria-forbidden-update-service@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);
        authenticateAs(requester);

        assertThatThrownBy(() -> ticketService.update(ticket.getId(), new UpdateTicketRequestDTO(
                null,
                null,
                null,
                TicketPriority.BAIXA,
                null,
                null
        )))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void cancelClosesTicketAndUpdatesTimestamp() throws Exception {
        User requester = saveUser("Maria Solicitante", "maria-cancel-ticket-service@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);
        Instant previousUpdatedAt = ticket.getUpdatedAt();
        authenticateAs(requester);

        Thread.sleep(10);
        ticketService.cancel(ticket.getId());

        Ticket cancelledTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(cancelledTicket.getStatus()).isEqualTo(TicketStatus.FECHADO);
        assertThat(cancelledTicket.getUpdatedAt()).isAfter(previousUpdatedAt);
        assertThat(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()))
                .extracting(TicketComment::getText)
                .containsExactly("Status alterado de ABERTO para FECHADO");
    }

    private void authenticateAs(User user) {
        Jwt jwt = jwtDecoder.decode(jwtTokenService.generateToken(user));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        ));
    }

    private User saveUser(String name, String email, UserRole role) {
        return userRepository.save(new User(name, email, "hash", role));
    }

    private Ticket saveTicket(
            String title,
            TicketCategory category,
            TicketPriority priority,
            User requester
    ) {
        return ticketRepository.saveAndFlush(new Ticket(
                title,
                "Descricao do chamado",
                category,
                priority,
                ClassificationOrigin.MANUAL,
                requester,
                null
        ));
    }

    private Ticket saveTicketWithStatus(
            String title,
            TicketCategory category,
            TicketPriority priority,
            User requester,
            TicketStatus status
    ) {
        Ticket ticket = saveTicket(title, category, priority, requester);
        ticket.setStatus(status);
        return ticketRepository.saveAndFlush(ticket);
    }
}
