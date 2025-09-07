package com.waduclay.newsletteragentspringai;

import com.waduclay.newsletteragentspringai.tavily.TavilyProperties;
import com.waduclay.newsletteragentspringai.workflow.WorkflowOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Main Spring Boot application class for the Newsletter Agent Spring AI system.
 * <p>
 * This application provides an AI-powered newsletter generation system that automatically:
 * <ul>
 *   <li>Searches the web for relevant topics using Tavily API</li>
 *   <li>Plans newsletter content using AI agents</li>
 *   <li>Writes newsletter sections using AI-powered writing agents</li>
 *   <li>Edits and finalizes the newsletter content</li>
 *   <li>Distributes newsletters via email</li>
 * </ul>
 * 
 * <p>The application leverages Spring AI framework with OpenAI models (via OpenRouter) 
 * to coordinate multiple AI agents in a workflow-based approach for automated newsletter creation.
 * 
 * <p><strong>Key Features:</strong>
 * <ul>
 *   <li>Multi-agent AI workflow orchestration</li>
 *   <li>Web search integration via Tavily API</li>
 *   <li>Automated email delivery</li>
 *   <li>Configurable scheduling for regular newsletter generation</li>
 * </ul>
 *
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(TavilyProperties.class)
public class NewsletterAgentSpringAiApplication {

    /**
     * Main entry point for the Newsletter Agent Spring AI application.
     * <p>
     * Bootstraps the Spring Boot application context and starts the embedded web server.
     * The application will automatically configure all beans and services needed for 
     * AI-powered newsletter generation.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(NewsletterAgentSpringAiApplication.class, args);
    }

    /**
     * Creates an ApplicationRunner bean that automatically triggers newsletter creation on startup.
     * <p>
     * <strong>Note:</strong> This bean is currently commented out to prevent automatic execution.
     * Uncomment the @Bean annotation to enable automatic newsletter generation on application startup.
     * When enabled, this will trigger the complete newsletter workflow including:
     * <ul>
     *   <li>Web search for relevant topics</li>
     *   <li>AI-powered content planning</li>
     *   <li>Section writing and editing</li>
     *   <li>Email distribution</li>
     * </ul>
     *
     * @param workflowOrchestrator the orchestrator responsible for coordinating the newsletter creation workflow
     * @return ApplicationRunner that executes the newsletter creation process
     */
//    @Bean
    ApplicationRunner applicationRunner(WorkflowOrchestrator workflowOrchestrator) {
        return args -> workflowOrchestrator.createNewsletter();
    }
}
