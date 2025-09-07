package com.waduclay.newsletteragentspringai.agent.planning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Agent responsible for planning newsletter topics based on web search results.
 *
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@Slf4j
@Component
public class PlanningAgent {

    private static final String RESULTS_SEPARATOR = "\n\n";

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final SystemMessage systemMessage;
    private final TopicFormatConverter topicFormatConverter;


    public PlanningAgent(ChatClient.Builder chatClientBuilder,
                         ObjectMapper objectMapper,
                         @Value("classpath:prompts/planning-agent-system-message.st")
                         Resource planningAgentSystemMessage, TopicFormatConverter topicFormatConverter
    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.systemMessage = new SystemMessage(planningAgentSystemMessage);
        this.topicFormatConverter = topicFormatConverter;
    }

    /**
     * Plan newsletter topics based on the provided search response.
     *
     * @param searchResponse the search response containing results to analyse
     * @return formatted topics for newsletter planning
     * @throws PlanningException if planning fails due to processing errors
     */
    public TopicFormat plan(TavilyResponse searchResponse) {
        validateInput(searchResponse);
        try {
            String serializedResults = serializeResults(searchResponse.getResults());
            return generateTopics(serializedResults);
        } catch (Exception e) {
            log.error("Failed to plan topics for search response: {}", searchResponse.getQuery(), e);
            throw new PlanningException("Failed to plan topics", e);
        }
    }


    private void validateInput(TavilyResponse searchResponse) {
        if (searchResponse == null) {
            throw new IllegalArgumentException("Search response cannot be null");
        }
        if (searchResponse.getResults() == null || searchResponse.getResults().isEmpty()) {
            throw new IllegalArgumentException("Search response must contain results");
        }
    }

    private String serializeResults(List<ResultsItem> results) {
        return results.parallelStream()
                .filter(Objects::nonNull)
                .map(this::safeSerialize)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(RESULTS_SEPARATOR));
    }

    private String safeSerialize(ResultsItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize result item: {}", item, e);
            return null; // Skip problematic items rather than failing entire operation
        }
    }

    private TopicFormat generateTopics(String serializedResults) {
        UserMessage userMessage = new UserMessage(serializedResults);
        Prompt prompt = new Prompt(systemMessage, userMessage);

        return Objects.requireNonNull(chatClient.prompt(prompt)
                .call()
                .entity(topicFormatConverter)
        );

    }
}
