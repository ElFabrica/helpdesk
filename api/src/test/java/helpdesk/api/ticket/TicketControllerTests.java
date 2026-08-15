package helpdesk.api.ticket;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void createTicketRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sistema financeiro fora do ar",
                                  "description": "Nao consigo acessar o sistema financeiro desde cedo."
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTicketValidatesRequiredFields() throws Exception {
        User requester = saveRequester();

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTicketForAuthenticatedUser() throws Exception {
        User requester = saveRequester();

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sistema financeiro fora do ar",
                                  "description": "Nao consigo acessar o sistema financeiro desde cedo."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sistema financeiro fora do ar"))
                .andExpect(jsonPath("$.description").value("Nao consigo acessar o sistema financeiro desde cedo."))
                .andExpect(jsonPath("$.status").value(TicketStatus.ABERTO.name()))
                .andExpect(jsonPath("$.category").value(TicketCategory.SOFTWARE.name()))
                .andExpect(jsonPath("$.priority").value(TicketPriority.ALTA.name()))
                .andExpect(jsonPath("$.classificationOrigin").value(ClassificationOrigin.IA.name()))
                .andExpect(jsonPath("$.requesterId").value(requester.getId()))
                .andExpect(jsonPath("$.responsibleId").doesNotExist());
    }

    @Test
    void listTicketsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listTicketsReturnsOnlyAuthenticatedRequesterTickets() throws Exception {
        User maria = saveUser("Maria Solicitante", "maria-list-controller@example.com", UserRole.SOLICITANTE);
        User joao = saveUser("Joao Solicitante", "joao-list-controller@example.com", UserRole.SOLICITANTE);
        Ticket mariaTicket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, maria);
        saveTicket("Acesso ao VPN", TicketCategory.ACESSO, TicketPriority.MEDIA, joao);

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(maria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(mariaTicket.getId()))
                .andExpect(jsonPath("$[0].title").value("Sistema lento"))
                .andExpect(jsonPath("$[0].requesterId").value(maria.getId()))
                .andExpect(jsonPath("$[0].description").doesNotExist())
                .andExpect(jsonPath("$[0].classificationOrigin").doesNotExist())
                .andExpect(jsonPath("$[0].updatedAt").doesNotExist());
    }

    @Test
    void adminCanListTicketsWithCombinedFilters() throws Exception {
        User admin = saveUser("Ana Admin", "ana-list-controller@example.com", UserRole.ADMIN);
        User maria = saveUser("Maria Solicitante", "maria-filter-controller@example.com", UserRole.SOLICITANTE);
        Ticket matchingTicket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, maria);
        saveTicket("Troca de monitor", TicketCategory.HARDWARE, TicketPriority.ALTA, maria);
        saveTicket("Sistema com lentidao baixa", TicketCategory.SOFTWARE, TicketPriority.BAIXA, maria);

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(admin))
                        .param("status", TicketStatus.ABERTO.name())
                        .param("priority", TicketPriority.ALTA.name())
                        .param("category", TicketCategory.SOFTWARE.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(matchingTicket.getId()))
                .andExpect(jsonPath("$[0].category").value(TicketCategory.SOFTWARE.name()))
                .andExpect(jsonPath("$[0].priority").value(TicketPriority.ALTA.name()))
                .andExpect(jsonPath("$[0].status").value(TicketStatus.ABERTO.name()));
    }

    @Test
    void listTicketsReturnsBadRequestForInvalidFilter() throws Exception {
        User requester = saveRequester();

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester))
                        .param("status", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    private User saveRequester() {
        return saveUser("Maria Solicitante", "maria-ticket-controller@example.com", UserRole.SOLICITANTE);
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
}
