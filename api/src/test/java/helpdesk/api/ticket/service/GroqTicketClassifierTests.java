package helpdesk.api.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;

import helpdesk.api.ticket.entity.ClassificationOrigin;
import helpdesk.api.ticket.entity.TicketCategory;
import helpdesk.api.ticket.entity.TicketPriority;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

class GroqTicketClassifierTests {

    @Test
    void classifiesTicketUsingGroqResponseWhenApiKeyIsConfigured() {
        AtomicReference<String> authorizationHeader = new AtomicReference<>();
        GroqTicketClassifier classifier = classifier(
                properties("test-key"),
                authorizationHeader,
                () -> """
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "{\\"category\\":\\"SOFTWARE\\",\\"priority\\":\\"BAIXA\\"}"
                              }
                            }
                          ]
                        }
                        """
        );

        ClassificationResult result = classifier.classify(
                "Internet fora do ar",
                "A rede esta indisponivel"
        );

        assertThat(result.category()).isEqualTo(TicketCategory.SOFTWARE);
        assertThat(result.priority()).isEqualTo(TicketPriority.BAIXA);
        assertThat(result.origin()).isEqualTo(ClassificationOrigin.IA);
        assertThat(authorizationHeader.get()).isEqualTo("Bearer test-key");
    }

    @Test
    void fallsBackToHeuristicWhenApiKeyIsMissing() {
        GroqClassifierProperties properties = new GroqClassifierProperties();
        properties.setApiKey("");

        GroqTicketClassifier classifier = classifier(properties);

        ClassificationResult result = classifier.classify(
                "Problema de login",
                "Nao consigo acessar o sistema"
        );

        assertThat(result.category()).isEqualTo(TicketCategory.ACESSO);
        assertThat(result.priority()).isEqualTo(TicketPriority.MEDIA);
        assertThat(result.origin()).isEqualTo(ClassificationOrigin.IA);
    }

    @Test
    void fallsBackToHeuristicWhenGroqResponseIsInvalid() {
        GroqTicketClassifier classifier = classifier(
                properties("test-key"),
                new AtomicReference<>(),
                () -> """
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "{\\"category\\":\\"INVALIDA\\",\\"priority\\":\\"ALTA\\"}"
                              }
                            }
                          ]
                        }
                        """
        );

        ClassificationResult result = classifier.classify(
                "Sistema financeiro fora do ar",
                "Servico indisponivel desde cedo"
        );

        assertThat(result.category()).isEqualTo(TicketCategory.SOFTWARE);
        assertThat(result.priority()).isEqualTo(TicketPriority.ALTA);
        assertThat(result.origin()).isEqualTo(ClassificationOrigin.IA);
    }

    private GroqTicketClassifier classifier(GroqClassifierProperties properties) {
        return new GroqTicketClassifier(
                restClient(new AtomicReference<>(), () -> "{}"),
                new ObjectMapper(),
                properties,
                new HeuristicTicketClassifier()
        );
    }

    private GroqTicketClassifier classifier(
            GroqClassifierProperties properties,
            AtomicReference<String> authorizationHeader,
            Supplier<String> responseBody
    ) {
        return new GroqTicketClassifier(
                restClient(authorizationHeader, responseBody),
                new ObjectMapper(),
                properties,
                new HeuristicTicketClassifier()
        );
    }

    private RestClient restClient(AtomicReference<String> authorizationHeader, Supplier<String> responseBody) {
        return RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .requestFactory(new StubClientHttpRequestFactory(authorizationHeader, responseBody))
                .build();
    }

    private GroqClassifierProperties properties(String apiKey) {
        GroqClassifierProperties properties = new GroqClassifierProperties();
        properties.setApiKey(apiKey);
        properties.setTimeout(Duration.ofSeconds(2));
        return properties;
    }

    private static class StubClientHttpRequestFactory implements ClientHttpRequestFactory {

        private final AtomicReference<String> authorizationHeader;
        private final Supplier<String> responseBody;

        private StubClientHttpRequestFactory(
                AtomicReference<String> authorizationHeader,
                Supplier<String> responseBody
        ) {
            this.authorizationHeader = authorizationHeader;
            this.responseBody = responseBody;
        }

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) {
            return new AbstractClientHttpRequest() {
                @Override
                protected OutputStream getBodyInternal(HttpHeaders headers) {
                    return OutputStream.nullOutputStream();
                }

                @Override
                protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
                    authorizationHeader.set(headers.getFirst(HttpHeaders.AUTHORIZATION));
                    MockClientHttpResponse response = new MockClientHttpResponse(
                            responseBody.get().getBytes(StandardCharsets.UTF_8),
                            HttpStatus.OK
                    );
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return response;
                }

                @Override
                public HttpMethod getMethod() {
                    return httpMethod;
                }

                @Override
                public URI getURI() {
                    return uri;
                }
            };
        }
    }
}
