package helpdesk.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT_SECURITY_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI helpdeskOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Helpdesk API")
                        .description("API REST para central de chamados com triagem automatica e dashboard em tempo real.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(JWT_SECURITY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEME));
    }
}
