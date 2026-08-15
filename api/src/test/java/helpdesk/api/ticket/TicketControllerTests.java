package helpdesk.api.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import helpdesk.api.ticket.repository.TicketCommentRepository;
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
    private TicketCommentRepository ticketCommentRepository;

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

    @Test
    void getTicketDetailReturnsOwnTicket() throws Exception {
        User requester = saveUser("Maria Solicitante", "maria-detail-controller@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);

        mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticket.getId()))
                .andExpect(jsonPath("$.title").value("Sistema lento"))
                .andExpect(jsonPath("$.description").value("Descricao do chamado"))
                .andExpect(jsonPath("$.requesterId").value(requester.getId()));
    }

    @Test
    void getTicketDetailReturnsNotFoundWhenTicketDoesNotExist() throws Exception {
        User requester = saveRequester();

        mockMvc.perform(get("/api/tickets/{id}", 999_999L)
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTicketDetailForAnotherRequesterReturnsForbidden() throws Exception {
        User maria = saveUser("Maria Solicitante", "maria-forbidden-detail-controller@example.com", UserRole.SOLICITANTE);
        User joao = saveUser("Joao Solicitante", "joao-forbidden-detail-controller@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, joao);

        mockMvc.perform(get("/api/tickets/{id}", ticket.getId())
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(maria)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUpdatesTicketFields() throws Exception {
        User admin = saveUser("Ana Admin", "ana-update-controller@example.com", UserRole.ADMIN);
        User requester = saveUser("Maria Solicitante", "maria-update-controller@example.com", UserRole.SOLICITANTE);
        User responsible = saveUser("Bruno Admin", "bruno-update-controller@example.com", UserRole.ADMIN);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);

        mockMvc.perform(patch("/api/tickets/{id}", ticket.getId())
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "EM_ANDAMENTO",
                                  "priority": "MEDIA",
                                  "category": "REDE",
                                  "responsibleId": %d
                                }
                                """.formatted(responsible.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TicketStatus.EM_ANDAMENTO.name()))
                .andExpect(jsonPath("$.priority").value(TicketPriority.MEDIA.name()))
                .andExpect(jsonPath("$.category").value(TicketCategory.REDE.name()))
                .andExpect(jsonPath("$.responsibleId").value(responsible.getId()));
    }

    @Test
    void requesterCannotUpdateAdminFields() throws Exception {
        User requester = saveUser("Maria Solicitante", "maria-forbidden-update-controller@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);

        mockMvc.perform(patch("/api/tickets/{id}", ticket.getId())
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "BAIXA"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCorrectsTicketClassification() throws Exception {
        User admin = saveUser("Ana Admin", "ana-classification-controller@example.com", UserRole.ADMIN);
        User requester = saveUser("Maria Solicitante", "maria-classification-controller@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicketWithOrigin(
                "Sistema lento",
                TicketCategory.SOFTWARE,
                TicketPriority.ALTA,
                ClassificationOrigin.IA,
                requester
        );

        mockMvc.perform(patch("/api/tickets/{id}/classification", ticket.getId())
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "REDE",
                                  "priority": "MEDIA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value(TicketCategory.REDE.name()))
                .andExpect(jsonPath("$.priority").value(TicketPriority.MEDIA.name()))
                .andExpect(jsonPath("$.classificationOrigin").value(ClassificationOrigin.MANUAL.name()));

        assertThat(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()))
                .extracting(comment -> comment.getText())
                .containsExactly("Classificacao alterada de SOFTWARE/ALTA para REDE/MEDIA");
    }

    @Test
    void requesterCannotCorrectTicketClassification() throws Exception {
        User requester = saveUser("Maria Solicitante", "maria-forbidden-classification-controller@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);

        mockMvc.perform(patch("/api/tickets/{id}/classification", ticket.getId())
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "REDE",
                                  "priority": "MEDIA"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTicketClassificationReturnsBadRequestForInvalidCategory() throws Exception {
        User admin = saveUser("Ana Admin", "ana-invalid-classification-controller@example.com", UserRole.ADMIN);
        User requester = saveUser("Maria Solicitante", "maria-invalid-classification-controller@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);

        mockMvc.perform(patch("/api/tickets/{id}/classification", ticket.getId())
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "INVALIDA",
                                  "priority": "MEDIA"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requesterCancelsOwnTicket() throws Exception {
        User requester = saveUser("Maria Solicitante", "maria-cancel-controller@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketCategory.SOFTWARE, TicketPriority.ALTA, requester);

        mockMvc.perform(delete("/api/tickets/{id}", ticket.getId())
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester)))
                .andExpect(status().isNoContent());

        Ticket cancelledTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(cancelledTicket.getStatus()).isEqualTo(TicketStatus.FECHADO);
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
        return saveTicketWithOrigin(title, category, priority, ClassificationOrigin.MANUAL, requester);
    }

    private Ticket saveTicketWithOrigin(
            String title,
            TicketCategory category,
            TicketPriority priority,
            ClassificationOrigin classificationOrigin,
            User requester
    ) {
        return ticketRepository.saveAndFlush(new Ticket(
                title,
                "Descricao do chamado",
                category,
                priority,
                classificationOrigin,
                requester,
                null
        ));
    }
}
