package com.waduclay.newsletteragentspringai;

import com.waduclay.newsletteragentspringai.tavily.TavilyProperties;
import com.waduclay.newsletteragentspringai.workflow.WorkflowOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(TavilyProperties.class)
public class NewsletterAgentSpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsletterAgentSpringAiApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(WorkflowOrchestrator workflowOrchestrator) {
        return args -> workflowOrchestrator.createNewsletter();
    }
}
