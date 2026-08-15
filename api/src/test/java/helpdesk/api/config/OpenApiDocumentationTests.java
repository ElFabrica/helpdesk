package helpdesk.api.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OpenApiDocumentationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocsArePublicAndIncludeMainEndpointsAndBearerJwt() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Helpdesk API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.paths['/api/auth/register']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/tickets']").exists())
                .andExpect(jsonPath("$.paths['/api/tickets/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/tickets/{ticketId}/comments']").exists())
                .andExpect(jsonPath("$.paths['/api/dashboard/indicators']").exists())
                .andExpect(jsonPath("$.paths['/api/dashboard/events']").exists());
    }

    @Test
    void swaggerUiIsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
