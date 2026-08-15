package helpdesk.api;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import helpdesk.api.auth.service.JwtTokenService;
import helpdesk.api.ticket.entity.ClassificationOrigin;
import helpdesk.api.ticket.entity.Ticket;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import helpdesk.api.ticket.entity.TicketStatus;
import helpdesk.api.ticket.repository.TicketRepository;
import helpdesk.api.user.entity.User;
import helpdesk.api.user.entity.UserRole;
import helpdesk.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CriticalApiBusinessRuleTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void registerCreatesRequesterWithoutExposingPasswordAndDuplicateEmailReturnsConflict() throws Exception {
        String payload = """
                {
                  "name": "Maria Silva",
                  "email": "maria-critical-register@example.com",
                  "password": "123456"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria-critical-register@example.com"))
                .andExpect(jsonPath("$.role").value(UserRole.SOLICITANTE.name()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("E-mail ja cadastrado"));
    }

    @Test
    void loginReturnsBearerTokenForValidCredentials() throws Exception {
        saveUser("Maria Login", "maria-critical-login@example.com", UserRole.SOLICITANTE, "123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "maria-critical-login@example.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void protectedRouteWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sistema financeiro fora do ar",
                                  "description": "Nao consigo acessar o sistema financeiro desde cedo."
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticacao necessaria"));
    }

    @Test
    void requesterCannotAccessAnotherRequesterTicket() throws Exception {
        User maria = saveUser("Maria Solicitante", "maria-critical-forbidden@example.com", UserRole.SOLICITANTE);
        User joao = saveUser("Joao Solicitante", "joao-critical-forbidden@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket(
                "Sistema lento",
                TicketPriority.ALTA,
                TicketStatus.ABERTO,
                joao
        );

        mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
                        .header("Authorization", bearer(maria)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Perfil sem permissao para esta operacao"));
    }

    @Test
    void createTicketAppliesAutomaticClassification() throws Exception {
        User requester = saveUser("Maria Solicitante", "maria-critical-ticket@example.com", UserRole.SOLICITANTE);

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", bearer(requester))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sistema financeiro fora do ar",
                                  "description": "Nao consigo acessar o sistema financeiro desde cedo."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sistema financeiro fora do ar"))
                .andExpect(jsonPath("$.status").value(TicketStatus.ABERTO.name()))
                .andExpect(jsonPath("$.category").value(TicketCategory.SOFTWARE.name()))
                .andExpect(jsonPath("$.priority").value(TicketPriority.ALTA.name()))
                .andExpect(jsonPath("$.classificationOrigin").value(ClassificationOrigin.IA.name()))
                .andExpect(jsonPath("$.requesterId").value(requester.getId()));
    }

    @Test
    void closedTicketCannotBeReopened() throws Exception {
        User requester = saveUser("Maria Solicitante", "maria-critical-reopen@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket(
                "Sistema lento",
                TicketPriority.ALTA,
                TicketStatus.FECHADO,
                requester
        );

        mockMvc.perform(patch("/api/tickets/{id}", ticket.getId())
                        .header("Authorization", bearer(requester))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "ABERTO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transicao de status invalida"));

        mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
                        .header("Authorization", bearer(requester)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TicketStatus.FECHADO.name()));
    }

    @Test
    void adminReadsDashboardIndicators() throws Exception {
        User admin = saveUser("Ana Admin", "ana-critical-dashboard@example.com", UserRole.ADMIN);
        User requester = saveUser("Maria Solicitante", "maria-critical-dashboard@example.com", UserRole.SOLICITANTE);
        saveTicket("Sistema indisponivel", TicketPriority.ALTA, TicketStatus.ABERTO, requester);
        saveTicket("VPN instavel", TicketPriority.MEDIA, TicketStatus.EM_ANDAMENTO, requester);
        saveTicket("Senha expirada", TicketPriority.MEDIA, TicketStatus.RESOLVIDO, requester);
        saveTicket("Troca de monitor", TicketPriority.BAIXA, TicketStatus.FECHADO, requester);

        mockMvc.perform(get("/api/dashboard/indicators")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.byStatus.ABERTO").value(1))
                .andExpect(jsonPath("$.byStatus.EM_ANDAMENTO").value(1))
                .andExpect(jsonPath("$.byStatus.RESOLVIDO").value(1))
                .andExpect(jsonPath("$.byStatus.FECHADO").value(1))
                .andExpect(jsonPath("$.byPriority.BAIXA").value(1))
                .andExpect(jsonPath("$.byPriority.MEDIA").value(2))
                .andExpect(jsonPath("$.byPriority.ALTA").value(1));
    }

    @Test
    void requesterCannotReadDashboardIndicators() throws Exception {
        User requester = saveUser("Maria Solicitante", "maria-critical-dashboard-forbidden@example.com", UserRole.SOLICITANTE);

        mockMvc.perform(get("/api/dashboard/indicators")
                        .header("Authorization", bearer(requester)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(not("")));
    }

    private User saveUser(String name, String email, UserRole role) {
        return saveUser(name, email, role, "123456");
    }

    private User saveUser(String name, String email, UserRole role, String password) {
        return userRepository.save(new User(
                name,
                email,
                passwordEncoder.encode(password),
                role
        ));
    }

    private Ticket saveTicket(
            String title,
            TicketPriority priority,
            TicketStatus status,
            User requester
    ) {
        Ticket ticket = new Ticket(
                title,
                "Descricao do chamado",
                TicketCategory.SOFTWARE,
                priority,
                ClassificationOrigin.MANUAL,
                requester,
                null
        );
        ticket.setStatus(status);

        return ticketRepository.saveAndFlush(ticket);
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }
}
