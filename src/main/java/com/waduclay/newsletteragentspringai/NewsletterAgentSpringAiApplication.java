package com.waduclay.newsletteragentspringai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.waduclay.newsletteragentspringai.tavily.*;
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
    ApplicationRunner runner(TavilyService service) throws JsonProcessingException {
        TavilySearch request = TavilySearch.builder()
                .query("spring boot")
                .timeRange(TimeRange.month)
                .includeRawContent(RawContent.text)
                .build();

        log.info("Request: {}", request);
        service.search(request);
        return args -> {

        };
    }

}
