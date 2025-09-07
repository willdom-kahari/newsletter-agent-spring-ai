package com.waduclay.newsletteragentspringai.agent.editor;


import com.waduclay.newsletteragentspringai.agent.writer.SectionFormatConverter;
import com.waduclay.newsletteragentspringai.email.EmailService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

/**
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@Component
public class EditorAgent {

    private final ChatClient chatClient;
    private final EmailService emailService;
    private final SectionFormatConverter sectionFormatConverter;
    @Value("classpath:prompts/editor-agent-system-message.st")
    Resource editorAgentSystemMessage;
    @Value("classpath:prompts/editor-agent-prompt-template.st")
    Resource editorAgentPromptTemplate;


    public EditorAgent(ChatClient.Builder chatClientBuilder,
                       EmailService emailService, SectionFormatConverter sectionFormatConverter) {
        this.chatClient = chatClientBuilder.build();
        this.emailService = emailService;
        this.sectionFormatConverter = sectionFormatConverter;
    }
    public String edit(String formattedMessage, String topic) throws IOException {
        String editorMessage = editorAgentSystemMessage.getContentAsString(StandardCharsets.UTF_8)
                .replace("{date}", LocalDate.now().toString());
        String userTemplate = editorAgentPromptTemplate.getContentAsString(StandardCharsets.UTF_8)
                .replace("{title}", topic)
                .replace("{sections}", formattedMessage);
        SystemMessage systemMessage = new SystemMessage(editorMessage);
        UserMessage userMessage = new UserMessage(userTemplate);
        Prompt prompt = new Prompt(systemMessage, userMessage);
        return Objects.requireNonNull(chatClient.prompt(prompt)
                        .tools(emailService)
                        .call()
                        .entity(sectionFormatConverter)
        );
    }
}
