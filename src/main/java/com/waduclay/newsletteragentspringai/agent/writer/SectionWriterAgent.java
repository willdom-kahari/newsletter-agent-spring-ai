package com.waduclay.newsletteragentspringai.agent.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waduclay.newsletteragentspringai.agent.planning.PlanningException;
import com.waduclay.newsletteragentspringai.tavily.ResultsItem;
import com.waduclay.newsletteragentspringai.tavily.TavilyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI agent responsible for writing individual newsletter sections based on research and topics.
 * <p>
 * This agent takes search results from Tavily web searches and a specific topic, then generates
 * well-structured newsletter content sections using AI-powered writing capabilities. The agent
 * leverages Spring AI's {@link ChatClient} with configurable prompt templates to ensure consistent
 * and high-quality content generation.
 * 
 * <p><strong>Key Features:</strong>
 * <ul>
 *   <li>AI-powered content generation using OpenAI models via OpenRouter</li>
 *   <li>Configurable prompt templates for consistent writing style</li>
 *   <li>Robust error handling with graceful degradation for failed items</li>
 *   <li>JSON serialization of search results for AI processing</li>
 *   <li>Template-based message formatting with placeholder replacement</li>
 * </ul>
 * 
 * <p><strong>Workflow:</strong>
 * <ol>
 *   <li>Validates input search response and topic</li>
 *   <li>Serializes search results to JSON format</li>
 *   <li>Formats prompt template with research data and topic</li>
 *   <li>Generates content using AI chat client</li>
 *   <li>Returns formatted section content</li>
 * </ol>
 *
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 * @since 1.0.0
 */
@Slf4j
@Component
public class SectionWriterAgent {

    private static final String RESULTS_SEPARATOR = "\n\n";

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final SystemMessage systemMessage;
    private final SectionFormatConverter sectionFormatConverter;
    @Value("classpath:prompts/section-writer-prompt-template.st")
    private Resource sectionWriterAgentPromptTemplate;

    /**
     * Constructs a new SectionWriterAgent with the required dependencies.
     * <p>
     * All dependencies are injected via Spring's dependency injection mechanism.
     * The system message template is loaded from the classpath and used to configure
     * the AI agent's behavior and writing style.
     *
     * @param chatClientBuilder builder for creating the Spring AI chat client
     * @param objectMapper JSON object mapper for serializing search results
     * @param sectionWriterAgentSystemMessage system message template resource for AI prompting
     * @param sectionFormatConverter converter for formatting the AI response output
     */
    public SectionWriterAgent(ChatClient.Builder chatClientBuilder,
                              ObjectMapper objectMapper,
                              @Value("classpath:prompts/section-writer-agent-system-message.st")
                              Resource sectionWriterAgentSystemMessage, SectionFormatConverter sectionFormatConverter

    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.systemMessage = new SystemMessage(sectionWriterAgentSystemMessage);
        this.sectionFormatConverter = sectionFormatConverter;
    }

    /**
     * Writes a newsletter section based on the provided search response and topic.
     * <p>
     * This method processes web search results and generates well-structured newsletter content
     * for a specific topic. The process involves:
     * <ol>
     *   <li>Validating the input search response</li>
     *   <li>Serializing search results to JSON format for AI processing</li>
     *   <li>Loading and formatting the prompt template with research data and topic</li>
     *   <li>Using the AI chat client to generate section content</li>
     *   <li>Converting the response using the section format converter</li>
     * </ol>
     *
     * @param searchResponse the Tavily search response containing relevant web results
     * @param topic the specific topic for which to write the newsletter section
     * @return formatted newsletter section content as a string
     * @throws IOException if template loading or processing fails
     * @throws IllegalArgumentException if the search response is null or empty
     */
    public String write(TavilyResponse searchResponse, String topic) throws IOException {
        validateInput(searchResponse);
        String serializedResults = serializeResults(searchResponse.getResults());
        String userMessageTemplate = sectionWriterAgentPromptTemplate.getContentAsString(StandardCharsets.UTF_8);
        String formattedMessage = userMessageTemplate.replace("{research}", serializedResults)
                .replace("{topic}", topic);
        UserMessage userMessage = new UserMessage(formattedMessage);
        Prompt prompt = new Prompt(systemMessage, userMessage);
        return Objects.requireNonNull(chatClient.prompt(prompt)
                .call()
                .entity(sectionFormatConverter)
        );

    }

    /**
     * Serializes a list of search result items into a formatted string for AI processing.
     * <p>
     * This method processes search results in parallel for performance, filtering out null items
     * and gracefully handling serialization failures. Failed items are logged and skipped
     * rather than causing the entire operation to fail.
     *
     * @param results the list of search result items to serialize
     * @return concatenated JSON strings separated by double newlines
     */
    private String serializeResults(List<ResultsItem> results) {
        return results.parallelStream()
                .filter(Objects::nonNull)
                .map(this::safeSerialize)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(RESULTS_SEPARATOR));
    }

    /**
     * Safely serializes a single result item to JSON string with error handling.
     * <p>
     * This method provides fault tolerance by catching JSON processing exceptions
     * and returning null for problematic items, allowing the overall process to continue.
     * Failed serialization attempts are logged for debugging purposes.
     *
     * @param item the result item to serialize
     * @return JSON string representation of the item, or null if serialization fails
     */
    private String safeSerialize(ResultsItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize result item: {}", item, e);
            return null; // Skip problematic items rather than failing entire operation
        }
    }

    /**
     * Validates the input search response to ensure it contains required data.
     * <p>
     * This method performs defensive programming by checking that the search response
     * is not null and contains at least one result item for processing.
     *
     * @param searchResponse the search response to validate
     * @throws IllegalArgumentException if the search response is null or contains no results
     */
    private void validateInput(TavilyResponse searchResponse) {
        if (searchResponse == null) {
            throw new IllegalArgumentException("Search response cannot be null");
        }
        if (searchResponse.getResults() == null || searchResponse.getResults().isEmpty()) {
            throw new IllegalArgumentException("Search response must contain results");
        }
    }
}
