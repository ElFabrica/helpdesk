package helpdesk.api.ticket.service;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai.groq")
public class GroqClassifierProperties {

    private boolean enabled = true;
    private String apiKey = "";
    private String baseUrl = "https://api.groq.com/openai/v1";
    private String model = "openai/gpt-oss-20b";
    private Duration timeout = Duration.ofSeconds(3);
}
