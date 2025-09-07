package com.waduclay.newsletteragentspringai.agent.planning;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waduclay.newsletteragentspringai.tavily.TavilyResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@Component
public class PlanningAgent {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    @Value("classpath:prompts/planning-agent-system-message.st")
    private Resource planningAgentSystemMessage;

    public PlanningAgent(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder
                .build();
        this.objectMapper = objectMapper;
    }

    public TopicFormat plan(TavilyResponse searchResponse) {

        String serializedResults = searchResponse.getResults().stream()
                .map(input -> {
                    try {
                        return objectMapper.writeValueAsString(searchResponse);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining("\n\n"));
        SystemMessage systemMessage = new SystemMessage(planningAgentSystemMessage);
        UserMessage userMessage = new UserMessage(serializedResults);

        Prompt prompt = new Prompt(systemMessage, userMessage);

        return chatClient.prompt(prompt)
                .call()
                .entity(TopicFormat.class);
    }
}
