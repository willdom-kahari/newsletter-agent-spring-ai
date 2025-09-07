package com.waduclay.newsletteragentspringai.workflow;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.waduclay.newsletteragentspringai.agent.editor.EditorAgent;
import com.waduclay.newsletteragentspringai.agent.planning.PlanningAgent;
import com.waduclay.newsletteragentspringai.agent.planning.TopicFormat;
import com.waduclay.newsletteragentspringai.agent.writer.SectionWriterAgent;
import com.waduclay.newsletteragentspringai.tavily.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@Slf4j
@Service
public class WorkflowOrchestrator {
    private final TavilyService tavilyService;
    private final PlanningAgent planningAgent;
    private final SectionWriterAgent sectionWriterAgent;
    private final EditorAgent editorAgent;

    public WorkflowOrchestrator(TavilyService tavilyService, PlanningAgent planningAgent, SectionWriterAgent sectionWriterAgent, EditorAgent editorAgent) {
        this.tavilyService = tavilyService;
        this.planningAgent = planningAgent;
        this.sectionWriterAgent = sectionWriterAgent;
        this.editorAgent = editorAgent;
    }

    @SneakyThrows
    @Scheduled(cron = "0 0 1 * * SUN")
    public void createNewsletter() {
        TavilySearch searchQuery = new TavilySearch("AI agents trends");
        TavilyResponse tavilyResponse = tavilyService.search(searchQuery);
        TopicFormat plannedTopics = planningAgent.plan(tavilyResponse);
        log.info("Planned topics: {}", plannedTopics);
        String newsletterSections = plannedTopics.topics().parallelStream()
                .map(this::searchForTopic)
                .map(query -> {
                    try {
                        return sectionWriterAgent.write(query.tavilyResponse, query.topic());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining("\n\n\n"));

        String response = editorAgent.edit(newsletterSections, plannedTopics.title());
        log.info("Editor response: {}", response);

    }

    private TavilySearch createTopicSearch(String topic) {
        TavilySearch tavilySearch = new TavilySearch(topic);
        tavilySearch.setTimeRange(TimeRange.month);
        tavilySearch.setIncludeRawContent(RawContent.text);
        tavilySearch.setMaxResults(3);
        return tavilySearch;
    }

    private Query searchForTopic(String topic) {
        TavilySearch topicSearch = createTopicSearch(topic);
        try {
            return new Query(tavilyService.search(topicSearch), topic);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to search for topic: " + topic, e);
        }
    }

    public record Query(TavilyResponse tavilyResponse, String topic) {
    }

}
