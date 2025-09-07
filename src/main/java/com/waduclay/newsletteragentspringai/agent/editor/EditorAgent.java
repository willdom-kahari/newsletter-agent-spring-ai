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
 * AI agent responsible for editing and finalizing newsletter content before distribution.
 * <p>
 * This agent serves as the final step in the newsletter creation workflow, taking all generated
 * sections and performing comprehensive editing tasks. The agent integrates with {@link EmailService}
 * as a Spring AI tool, enabling it to automatically distribute the finalized newsletter via email.
 * 
 * <p><strong>Key Responsibilities:</strong>
 * <ul>
 *   <li>Final editing and polishing of newsletter content</li>
 *   <li>Content structure and flow optimization</li>
 *   <li>Quality assurance and consistency checks</li>
 *   <li>Integration with email distribution system</li>
 *   <li>Date-aware content formatting</li>
 * </ul>
 * 
 * <p><strong>Workflow Integration:</strong>
 * <ul>
 *   <li>Receives aggregated sections from {@link com.waduclay.newsletteragentspringai.agent.writer.SectionWriterAgent}</li>
 *   <li>Applies editorial enhancements using AI-powered editing</li>
 *   <li>Formats content with current date context</li>
 *   <li>Utilizes {@link EmailService} tool for automatic distribution</li>
 * </ul>
 * 
 * <p><strong>Template Configuration:</strong>
 * The agent uses configurable prompt templates for consistent editing style and behavior,
 * with dynamic date injection for time-sensitive content formatting.
 *
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 * @since 1.0.0
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


    /**
     * Constructs a new EditorAgent with the required dependencies.
     * <p>
     * All dependencies are injected via Spring's dependency injection mechanism.
     * The EmailService is registered as a tool with the chat client, enabling the AI
     * to automatically send emails as part of the editing workflow.
     *
     * @param chatClientBuilder builder for creating the Spring AI chat client
     * @param emailService service for sending newsletters via email (registered as AI tool)
     * @param sectionFormatConverter converter for formatting the AI response output
     */
    public EditorAgent(ChatClient.Builder chatClientBuilder,
                       EmailService emailService, SectionFormatConverter sectionFormatConverter) {
        this.chatClient = chatClientBuilder.build();
        this.emailService = emailService;
        this.sectionFormatConverter = sectionFormatConverter;
    }

    /**
     * Edits and finalizes newsletter content, potentially triggering email distribution.
     * <p>
     * This method performs comprehensive editing of newsletter sections and can automatically
     * distribute the final newsletter via email. The process includes:
     * <ol>
     *   <li>Loading and formatting system message template with current date</li>
     *   <li>Creating user message with newsletter title and section content</li>
     *   <li>Processing content through AI chat client with EmailService tool integration</li>
     *   <li>Returning formatted final newsletter content</li>
     * </ol>
     * 
     * <p><strong>Tool Integration:</strong> The chat client has access to {@link EmailService}
     * as a tool, allowing the AI to decide when and how to send the newsletter based on
     * the editing context and instructions.
     * 
     * <p><strong>Date Context:</strong> The system message is dynamically updated with the
     * current date to provide temporal context for content editing.
     *
     * @param formattedMessage the aggregated newsletter sections to be edited
     * @param topic the main title/topic of the newsletter
     * @return finalized newsletter content after AI editing
     * @throws IOException if template loading fails
     */
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
