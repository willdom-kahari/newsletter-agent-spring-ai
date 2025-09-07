package com.waduclay.newsletteragentspringai.tavily;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Service for interacting with the Tavily web search API.
 * <p>
 * This service provides web search capabilities using the Tavily API, which specializes in
 * AI-optimized web search results. It handles authentication, request/response processing,
 * and provides structured search results for use by AI agents in content generation workflows.
 * 
 * <p><strong>Key Features:</strong>
 * <ul>
 *   <li>RESTful API integration with Tavily search service</li>
 *   <li>Bearer token authentication for secure access</li>
 *   <li>JSON request/response processing with comprehensive logging</li>
 *   <li>Configurable search parameters via {@link TavilySearch}</li>
 *   <li>Structured response mapping to {@link TavilyResponse}</li>
 * </ul>
 * 
 * <p><strong>Configuration:</strong>
 * The service is configured via {@link TavilyProperties} which includes:
 * <ul>
 *   <li>Base URL for the Tavily API endpoint</li>
 *   <li>API key for authentication (should be provided via environment variables)</li>
 * </ul>
 * 
 * <p><strong>Usage in Workflow:</strong>
 * This service is primarily used by {@link com.waduclay.newsletteragentspringai.workflow.WorkflowOrchestrator}
 * to perform initial topic searches and targeted research for newsletter content generation.
 *
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 * @since 1.0.0
 */
@Slf4j
@Service
public class TavilyService {
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    /**
     * Constructs a new TavilyService with the required dependencies.
     * <p>
     * Initializes the REST client with Tavily API configuration including base URL and
     * Bearer token authentication. All configuration is provided via {@link TavilyProperties}
     * which should be configured through application properties and environment variables.
     *
     * @param tavilyProperties configuration properties containing API base URL and key
     * @param objectMapper JSON object mapper for request/response serialization
     */
    public TavilyService(TavilyProperties tavilyProperties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(tavilyProperties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + tavilyProperties.apiKey())
                .build();
    }

    /**
     * Performs a web search using the Tavily API.
     * <p>
     * This method sends a search request to the Tavily API and returns structured search results.
     * The method includes comprehensive logging for both requests and responses to aid in
     * debugging and monitoring API interactions.
     * 
     * <p><strong>Process:</strong>
     * <ol>
     *   <li>Serializes the search request to JSON for logging</li>
     *   <li>Sends POST request to Tavily API with search parameters</li>
     *   <li>Logs the raw response for debugging purposes</li>
     *   <li>Deserializes response to {@link TavilyResponse} object</li>
     * </ol>
     *
     * @param request the search request containing query parameters and configuration
     * @return structured search response containing results and metadata
     * @throws JsonProcessingException if JSON serialization/deserialization fails
     */
    public TavilyResponse search(TavilySearch request) throws JsonProcessingException {
        String cleanedUpRequest = objectMapper.writeValueAsString(request);
        log.info("Tavily Request: {}", cleanedUpRequest);

        String body = restClient.post()
                .body(request)
                .retrieve()
                .body(String.class);
        log.debug("Tavily Response: {}", objectMapper.writeValueAsString(body));
        return objectMapper.readValue(body, TavilyResponse.class);
    }
}
