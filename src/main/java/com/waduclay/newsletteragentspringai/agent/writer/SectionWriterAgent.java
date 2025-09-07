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
 * Agent responsible for planning newsletter topics based on web search results.
 *
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@Slf4j
@Component
public class SectionWriterAgent {

    private static final String RESULTS_SEPARATOR = "\n\n";

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final SystemMessage systemMessage;
    @Value("classpath:prompts/section-writer-prompt-template.st")
    private Resource sectionWriterAgentPromptTemplate;
    private final SectionFormatConverter sectionFormatConverter;

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
     * Plan newsletter topics based on the provided search response.
     *
     * @return formatted topics for newsletter planning
     * @throws PlanningException if planning fails due to processing errors
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


    private void validateInput(TavilyResponse searchResponse) {
        if (searchResponse == null) {
            throw new IllegalArgumentException("Search response cannot be null");
        }
        if (searchResponse.getResults() == null || searchResponse.getResults().isEmpty()) {
            throw new IllegalArgumentException("Search response must contain results");
        }
    }
}
