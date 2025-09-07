package com.waduclay.newsletteragentspringai.tavily;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@Slf4j
@Service
public class TavilyService {
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public TavilyService(TavilyProperties tavilyProperties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(tavilyProperties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + tavilyProperties.apiKey())
                .build();
    }

    public TavilyResponse search(TavilySearch request) throws JsonProcessingException {
        String cleanedUpRequest = objectMapper.writeValueAsString(request);
        log.info("Tavily Request: {}", cleanedUpRequest);

        String body = restClient.post()
                .body(request)
                .retrieve()
                .body(String.class);
        log.debug("Tavily Response: {}", objectMapper.writeValueAsString(body));
        return objectMapper.readValue(body, TavilyResponse.class);
    }
}
