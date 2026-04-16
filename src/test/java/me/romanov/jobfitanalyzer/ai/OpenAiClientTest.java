package me.romanov.jobfitanalyzer.ai;

import me.romanov.jobfitanalyzer.config.OpenAiProperties;
import me.romanov.jobfitanalyzer.domain.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpenAiClientTest {

    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    private RestClient.ResponseSpec responseSpec;
    private OpenAiClient openAiClient;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAiProperties openAiProperties = new OpenAiProperties("test-api-key");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/chat/completions")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header("Authorization", "Bearer test-api-key")).thenReturn(requestBodySpec);
        when(requestBodySpec.header("Content-Type", "application/json")).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        openAiClient = new OpenAiClient(restClient, objectMapper, openAiProperties);
    }

    @Test
    void shouldThrowWhenRawResponseIsBlank() {
        when(responseSpec.body(String.class)).thenReturn("   ");

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> openAiClient.callOpenAi("system prompt", "user prompt")
        );

        assertTrue(exception.getMessage().contains("OpenAI request failed"));

        verify(restClient).post();
        verify(requestBodyUriSpec).uri("/chat/completions");
    }

    @Test
    void shouldThrowWhenResponseCannotBeParsed() {
        when(responseSpec.body(String.class)).thenReturn("{invalid json");

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> openAiClient.callOpenAi("system prompt", "user prompt")
        );

        assertTrue(exception.getMessage().contains("OpenAI request failed"));

        verify(restClient).post();
        verify(requestBodyUriSpec).uri("/chat/completions");
    }
}