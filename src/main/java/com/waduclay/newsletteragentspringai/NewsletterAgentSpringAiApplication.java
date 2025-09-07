package com.waduclay.newsletteragentspringai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waduclay.newsletteragentspringai.tavily.TavilyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class NewsletterAgentSpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsletterAgentSpringAiApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(ObjectMapper objectMapper) throws JsonProcessingException {
        TavilyRequest request = TavilyRequest.builder().query("test").build();
        String s = objectMapper.writeValueAsString(request);
        log.info(s);
        return args -> {

        };
    }

}
