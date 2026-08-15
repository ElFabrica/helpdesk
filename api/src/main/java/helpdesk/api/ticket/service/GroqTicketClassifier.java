package helpdesk.api.ticket.service;

import helpdesk.api.ticket.entity.ClassificationOrigin;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Primary
@Component
public class GroqTicketClassifier implements TicketClassifier {

    private static final Logger logger = LoggerFactory.getLogger(GroqTicketClassifier.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final GroqClassifierProperties properties;
    private final HeuristicTicketClassifier fallbackClassifier;

    @Autowired
    public GroqTicketClassifier(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            GroqClassifierProperties properties,
            HeuristicTicketClassifier fallbackClassifier
    ) {
        this(
                buildRestClient(restClientBuilder, properties),
                objectMapper,
                properties,
                fallbackClassifier
        );
    }

    GroqTicketClassifier(
            RestClient restClient,
            ObjectMapper objectMapper,
            GroqClassifierProperties properties,
            HeuristicTicketClassifier fallbackClassifier
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.fallbackClassifier = fallbackClassifier;
        this.restClient = restClient;
    }

    private static RestClient buildRestClient(
            RestClient.Builder restClientBuilder,
            GroqClassifierProperties properties
    ) {
        return restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory(properties))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public ClassificationResult classify(String title, String description) {
        if (!shouldUseGroq()) {
            return fallbackClassifier.classify(title, description);
        }

        try {
            ClassificationResult result = classifyWithGroq(title, description);
            return new ClassificationResult(result.category(), result.priority(), ClassificationOrigin.IA);
        } catch (RuntimeException exception) {
            logger.warn("Groq ticket classification failed. Falling back to local heuristic: {}", exception.toString());
            return fallbackClassifier.classify(title, description);
        }
    }

    private ClassificationResult classifyWithGroq(String title, String description) {
        GroqChatResponseDTO response = restClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .body(new GroqChatRequestDTO(
                        properties.getModel(),
                        List.of(
                                new GroqMessageDTO("system", systemPrompt()),
                                new GroqMessageDTO("user", userPrompt(title, description))
                        ),
                        0,
                        80,
                        new GroqResponseFormatDTO("json_schema", classificationSchema())
                ))
                .retrieve()
                .body(GroqChatResponseDTO.class);

        String content = extractContent(response);
        GroqClassificationPayloadDTO payload = parsePayload(content);

        return new ClassificationResult(
                TicketCategory.valueOf(payload.category()),
                TicketPriority.valueOf(payload.priority()),
                ClassificationOrigin.IA
        );
    }

    private String extractContent(GroqChatResponseDTO response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Groq response does not contain choices");
        }

        GroqChoiceDTO choice = response.choices().getFirst();
        if (choice.message() == null || !StringUtils.hasText(choice.message().content())) {
            throw new IllegalStateException("Groq response does not contain message content");
        }

        return choice.message().content();
    }

    private GroqClassificationPayloadDTO parsePayload(String content) {
        try {
            return objectMapper.readValue(content, GroqClassificationPayloadDTO.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Groq response content is not valid classification JSON", exception);
        }
    }

    private boolean shouldUseGroq() {
        return properties.isEnabled() && StringUtils.hasText(properties.getApiKey());
    }

    private static SimpleClientHttpRequestFactory requestFactory(GroqClassifierProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getTimeout());
        requestFactory.setReadTimeout(properties.getTimeout());
        return requestFactory;
    }

    private String systemPrompt() {
        return """
                Classifique chamados internos de helpdesk.
                Responda somente JSON valido, sem markdown.
                Use exatamente uma categoria: HARDWARE, SOFTWARE, ACESSO, REDE, OUTROS.
                Use exatamente uma prioridade: BAIXA, MEDIA, ALTA.
                Priorize ALTA para indisponibilidade, urgencia, parada ou impacto critico.
                """;
    }

    private String userPrompt(String title, String description) {
        return """
                Titulo: %s
                Descricao: %s
                """.formatted(title, description);
    }

    private GroqJsonSchemaDTO classificationSchema() {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("category", "priority"),
                "properties", Map.of(
                        "category", Map.of(
                                "type", "string",
                                "enum", List.of("HARDWARE", "SOFTWARE", "ACESSO", "REDE", "OUTROS")
                        ),
                        "priority", Map.of(
                                "type", "string",
                                "enum", List.of("BAIXA", "MEDIA", "ALTA")
                        )
                )
        );

        return new GroqJsonSchemaDTO("ticket_classification", true, schema);
    }

    public record GroqChatRequestDTO(
            String model,
            List<GroqMessageDTO> messages,
            int temperature,
            int max_completion_tokens,
            GroqResponseFormatDTO response_format
    ) {
    }

    public record GroqMessageDTO(String role, String content) {
    }

    public record GroqResponseFormatDTO(String type, GroqJsonSchemaDTO json_schema) {
    }

    public record GroqJsonSchemaDTO(String name, boolean strict, Map<String, Object> schema) {
    }

    public record GroqChatResponseDTO(List<GroqChoiceDTO> choices) {
    }

    public record GroqChoiceDTO(GroqMessageDTO message) {
    }

    public record GroqClassificationPayloadDTO(String category, String priority) {
    }
}
