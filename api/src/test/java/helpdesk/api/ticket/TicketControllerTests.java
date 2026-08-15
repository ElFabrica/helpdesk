package helpdesk.api.ticket;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import helpdesk.api.auth.JwtTokenService;
import helpdesk.api.user.User;
import helpdesk.api.user.UserRepository;
import helpdesk.api.user.UserRole;
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

    private User saveRequester() {
        return userRepository.save(new User(
                "Maria Solicitante",
                "maria-ticket-controller@example.com",
                "hash",
                UserRole.SOLICITANTE
        ));
    }
}
