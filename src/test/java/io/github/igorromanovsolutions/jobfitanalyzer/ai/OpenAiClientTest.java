package io.github.igorromanovsolutions.jobfitanalyzer.ai;

import io.github.igorromanovsolutions.jobfitanalyzer.config.OpenAiProperties;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.ExternalServiceException;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.AnalysisResult;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.OpenAiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiClientTest {

    private MockRestServiceServer server;
    private ObjectMapper objectMapper;
    private OpenAiClient openAiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("https://api.openai.com/v1");

        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        objectMapper = new ObjectMapper();

        RestClient restClient = restClientBuilder.build();
        OpenAiProperties openAiProperties = new OpenAiProperties("test-api-key");

        openAiClient = new OpenAiClient(restClient, objectMapper, openAiProperties);
    }

    @Test
    void shouldReturnAnalysisResultWhenOpenAiRespondsSuccessfully() {
        MDC.put("correlationId", "corr-123");

        AnalysisResult expected = new AnalysisResult();
        expected.setRoleType("BACKEND");
        expected.setJavaRelevance("HIGH");

        OpenAiResponse openAiResponse = new OpenAiResponse();
        OpenAiResponse.Choice choice = new OpenAiResponse.Choice();
        OpenAiResponse.Message message = new OpenAiResponse.Message();
        message.setContent(objectMapper.writeValueAsString(expected));
        choice.setMessage(message);
        openAiResponse.setChoices(List.of(choice));

        String rawResponse = objectMapper.writeValueAsString(openAiResponse);

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-api-key"))
                .andExpect(header("Content-Type", "application/json"))
                .andRespond(withSuccess(rawResponse, MediaType.APPLICATION_JSON));

        AnalysisResult actual = openAiClient.callOpenAi("system prompt", "user prompt");

        assertEquals("BACKEND", actual.getRoleType());
        assertEquals("HIGH", actual.getJavaRelevance());

        server.verify();
        MDC.clear();
    }

    @Test
    void shouldThrowWhenRawResponseIsBlank() {
        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("   ", MediaType.TEXT_PLAIN));

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> openAiClient.callOpenAi("system prompt", "user prompt")
        );

        assertTrue(exception.getMessage().contains("OpenAI request failed"));
        server.verify();
    }

    @Test
    void shouldThrowWhenResponseHasNoChoices() {
        OpenAiResponse openAiResponse = new OpenAiResponse();
        openAiResponse.setChoices(List.of());

        String rawResponse = objectMapper.writeValueAsString(openAiResponse);

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(rawResponse, MediaType.APPLICATION_JSON));

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> openAiClient.callOpenAi("system prompt", "user prompt")
        );

        assertTrue(exception.getMessage().contains("OpenAI response has no choices"));
        server.verify();
    }

    @Test
    void shouldThrowWhenResponseContentIsEmpty() {
        OpenAiResponse openAiResponse = new OpenAiResponse();
        OpenAiResponse.Choice choice = new OpenAiResponse.Choice();
        OpenAiResponse.Message message = new OpenAiResponse.Message();
        message.setContent("   ");
        choice.setMessage(message);
        openAiResponse.setChoices(List.of(choice));

        String rawResponse = objectMapper.writeValueAsString(openAiResponse);

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(rawResponse, MediaType.APPLICATION_JSON));

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> openAiClient.callOpenAi("system prompt", "user prompt")
        );

        assertTrue(exception.getMessage().contains("OpenAI response content is empty"));
        server.verify();
    }

    @Test
    void shouldThrowWhenNestedJsonCannotBeParsed() {
        OpenAiResponse openAiResponse = new OpenAiResponse();
        OpenAiResponse.Choice choice = new OpenAiResponse.Choice();
        OpenAiResponse.Message message = new OpenAiResponse.Message();
        message.setContent("{broken-json}");
        choice.setMessage(message);
        openAiResponse.setChoices(List.of(choice));

        String rawResponse = objectMapper.writeValueAsString(openAiResponse);

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(rawResponse, MediaType.APPLICATION_JSON));

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> openAiClient.callOpenAi("system prompt", "user prompt")
        );

        assertTrue(exception.getMessage().contains("Failed to parse OpenAI response"));
        server.verify();
    }
}