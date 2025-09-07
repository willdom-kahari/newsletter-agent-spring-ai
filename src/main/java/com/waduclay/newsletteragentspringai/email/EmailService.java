package com.waduclay.newsletteragentspringai.email;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Service for sending newsletters via email with Spring AI tool integration.
 * <p>
 * This service provides email functionality for newsletter distribution and is designed to work
 * seamlessly with Spring AI agents as a tool. The {@code @Tool} annotation enables AI agents
 * to automatically invoke email sending capabilities as part of their decision-making process.
 * 
 * <p><strong>Key Features:</strong>
 * <ul>
 *   <li>Spring AI tool integration via {@code @Tool} annotation</li>
 *   <li>HTML email support with MIME message handling</li>
 *   <li>Configurable sender and recipient via application properties</li>
 *   <li>UTF-8 encoding support for international content</li>
 *   <li>Comprehensive error handling with graceful degradation</li>
 *   <li>Detailed logging for monitoring email delivery</li>
 * </ul>
 * 
 * <p><strong>Configuration:</strong>
 * Email configuration is managed through Spring Boot's mail properties:
 * <ul>
 *   <li>{@code spring.mail.username} - Sender email address</li>
 *   <li>{@code spring.mail.recipient} - Recipient email address</li>
 *   <li>SMTP configuration via standard Spring Boot mail properties</li>
 * </ul>
 * 
 * <p><strong>AI Tool Integration:</strong>
 * This service is registered as a Spring AI tool, allowing AI agents (particularly
 * {@link com.waduclay.newsletteragentspringai.agent.editor.EditorAgent}) to automatically
 * send newsletters based on content and context analysis.
 *
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String emailSender;
    @Value("${spring.mail.recipient}")
    private String emailRecipient;


    /**
     * Sends an email with the specified subject and content.
     * <p>
     * This method is annotated with {@code @Tool} to enable Spring AI agents to automatically
     * invoke email sending capabilities. The AI can decide when and how to send newsletters
     * based on content analysis and workflow context.
     * 
     * <p><strong>Email Features:</strong>
     * <ul>
     *   <li>HTML content support for rich newsletter formatting</li>
     *   <li>UTF-8 encoding for international character support</li>
     *   <li>MIME multipart message handling</li>
     *   <li>Configurable sender and recipient addresses</li>
     * </ul>
     * 
     * <p><strong>Error Handling:</strong>
     * The method uses defensive programming with try-catch blocks to handle messaging
     * exceptions gracefully. Failed email attempts are logged but do not throw exceptions
     * to prevent disrupting the AI workflow.
     * 
     * <p><strong>Spring AI Tool Integration:</strong>
     * The {@code @Tool} annotation exposes this method to Spring AI agents with:
     * <ul>
     *   <li>Tool name: "sendEmail"</li>
     *   <li>Description: "Pass the subject and the content to send an email"</li>
     *   <li>Automatic parameter mapping for AI-driven invocation</li>
     * </ul>
     *
     * @param subject the email subject line
     * @param content the email body content (supports HTML formatting)
     * @throws MessagingException if critical email configuration or sending fails
     */
    @Tool(name = "sendEmail", description = "Pass the subject and the content to send an email")
    public void sendEmail(
            String subject,
            String content
    ) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_RELATED, StandardCharsets.UTF_8.name());
        messageHelper.setFrom(emailSender);

        messageHelper.setSubject(subject);

        try {
            messageHelper.setText(content, true);
            messageHelper.setTo(emailRecipient);
            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", emailRecipient);
        } catch (MessagingException e) {
            log.warn("Cannot send email to: {}", emailRecipient);
        }
    }

}
