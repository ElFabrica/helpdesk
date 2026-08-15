package helpdesk.api.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void indicatorsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/dashboard/indicators"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eventsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/dashboard/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requesterCannotAccessGlobalIndicators() throws Exception {
        User requester = saveUser("Maria Solicitante", "maria-dashboard-forbidden@example.com", UserRole.SOLICITANTE);

        mockMvc.perform(get("/api/dashboard/indicators")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester)))
                .andExpect(status().isForbidden());
    }

    @Test
    void requesterCannotAccessDashboardEvents() throws Exception {
        User requester = saveUser("Joao Solicitante", "joao-dashboard-events-forbidden@example.com", UserRole.SOLICITANTE);

        mockMvc.perform(get("/api/dashboard/events")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(requester)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanConnectToDashboardEvents() throws Exception {
        User admin = saveUser("Bruna Admin", "bruna-dashboard-events@example.com", UserRole.ADMIN);

        mockMvc.perform(get("/api/dashboard/events")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    void adminGetsDashboardIndicatorsWithAllEnumKeys() throws Exception {
        User admin = saveUser("Ana Admin", "ana-dashboard@example.com", UserRole.ADMIN);
        User requester = saveUser("Maria Solicitante", "maria-dashboard@example.com", UserRole.SOLICITANTE);
        saveTicket("Sistema indisponivel", TicketPriority.ALTA, TicketStatus.ABERTO, requester);
        saveTicket("VPN instavel", TicketPriority.MEDIA, TicketStatus.EM_ANDAMENTO, requester);
        saveTicket("Senha expirada", TicketPriority.MEDIA, TicketStatus.RESOLVIDO, requester);
        saveTicket("Troca de monitor", TicketPriority.BAIXA, TicketStatus.FECHADO, requester);

        mockMvc.perform(get("/api/dashboard/indicators")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(admin)))
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
    void changedTicketStatusChangesDashboardIndicators() throws Exception {
        User admin = saveUser("Bruno Admin", "bruno-dashboard@example.com", UserRole.ADMIN);
        User requester = saveUser("Joao Solicitante", "joao-dashboard@example.com", UserRole.SOLICITANTE);
        Ticket ticket = saveTicket("Sistema lento", TicketPriority.ALTA, TicketStatus.ABERTO, requester);
        ticket.setStatus(TicketStatus.FECHADO);
        ticketRepository.saveAndFlush(ticket);

        mockMvc.perform(get("/api/dashboard/indicators")
                        .header("Authorization", "Bearer " + jwtTokenService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.byStatus.ABERTO").value(0))
                .andExpect(jsonPath("$.byStatus.FECHADO").value(1))
                .andExpect(jsonPath("$.byPriority.ALTA").value(1));
    }

    private User saveUser(String name, String email, UserRole role) {
        return userRepository.save(new User(name, email, "hash", role));
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
}
