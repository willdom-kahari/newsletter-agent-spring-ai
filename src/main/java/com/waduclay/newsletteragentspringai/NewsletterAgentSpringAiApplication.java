package com.waduclay.newsletteragentspringai;

import com.waduclay.newsletteragentspringai.tavily.TavilyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(TavilyProperties.class)
public class NewsletterAgentSpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsletterAgentSpringAiApplication.class, args);
    }

}
