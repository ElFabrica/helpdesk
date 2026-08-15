package helpdesk.api.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import helpdesk.api.auth.JwtTokenService;
import helpdesk.api.user.User;
import helpdesk.api.user.UserRepository;
import helpdesk.api.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TicketServiceTests {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

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

    private void authenticateAs(User user) {
        Jwt jwt = jwtDecoder.decode(jwtTokenService.generateToken(user));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
