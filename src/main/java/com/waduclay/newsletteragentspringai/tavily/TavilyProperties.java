package com.waduclay.newsletteragentspringai.tavily;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@ConfigurationProperties(prefix = "tavily")
public record TavilyProperties(
        String apiKey,
        String baseUrl
) {
}
