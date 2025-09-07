package com.waduclay.newsletteragentspringai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.waduclay.newsletteragentspringai.email.EmailService;
import com.waduclay.newsletteragentspringai.tavily.*;
import jakarta.mail.MessagingException;
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
    ApplicationRunner runner(EmailService service) throws JsonProcessingException, MessagingException {
      service.sendEmail("Makadii Nyika", "Ndeip baba");
        return args -> {

        };
    }

}
