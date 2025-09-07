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
 * Central orchestrator for the AI-powered newsletter creation workflow.
 * <p>
 * This service coordinates multiple AI agents to automatically generate newsletters:
 * <ul>
 *   <li>{@link PlanningAgent} - Plans newsletter topics based on web search results</li>
 *   <li>{@link SectionWriterAgent} - Writes individual newsletter sections for each topic</li>
 *   <li>{@link EditorAgent} - Edits and finalizes the complete newsletter content</li>
 * </ul>
 * 
 * <p>The workflow operates in the following sequence:
 * <ol>
 *   <li>Performs initial web search for AI-related topics using {@link TavilyService}</li>
 *   <li>Uses {@link PlanningAgent} to analyze search results and plan newsletter structure</li>
 *   <li>For each planned topic, performs targeted searches and generates content sections</li>
 *   <li>Aggregates all sections and uses {@link EditorAgent} for final editing and email delivery</li>
 * </ol>
 * 
 * <p>The orchestrator supports both scheduled execution (weekly via cron) and manual invocation.
 * Content generation leverages parallel processing for improved performance when writing multiple sections.
 *
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 * @since 1.0.0
 */
@Slf4j
@Service
public class WorkflowOrchestrator {
    private final TavilyService tavilyService;
    private final PlanningAgent planningAgent;
    private final SectionWriterAgent sectionWriterAgent;
    private final EditorAgent editorAgent;

    /**
     * Constructs a new WorkflowOrchestrator with the required AI agents and services.
     * <p>
     * All dependencies are injected via Spring's dependency injection mechanism.
     *
     * @param tavilyService the service for performing web searches via Tavily API
     * @param planningAgent the AI agent responsible for planning newsletter topics
     * @param sectionWriterAgent the AI agent responsible for writing newsletter sections
     * @param editorAgent the AI agent responsible for editing and finalizing newsletter content
     */
    public WorkflowOrchestrator(TavilyService tavilyService, PlanningAgent planningAgent, SectionWriterAgent sectionWriterAgent, EditorAgent editorAgent) {
        this.tavilyService = tavilyService;
        this.planningAgent = planningAgent;
        this.sectionWriterAgent = sectionWriterAgent;
        this.editorAgent = editorAgent;
    }

    /**
     * Creates a complete newsletter using the AI agent workflow.
     * <p>
     * This method orchestrates the entire newsletter creation process:
     * <ol>
     *   <li>Performs initial search for "AI agent workflows project ideas" within the past week</li>
     *   <li>Uses {@link PlanningAgent} to analyze results and generate a structured topic plan</li>
     *   <li>For each planned topic, performs targeted searches and generates content sections in parallel</li>
     *   <li>Aggregates all sections and uses {@link EditorAgent} for final editing and distribution</li>
     * </ol>
     * 
     * <p><strong>Scheduling:</strong> This method is automatically executed every Sunday at 1:00 AM
     * as configured by the {@code @Scheduled} annotation.
     * 
     * <p><strong>Error Handling:</strong> Uses {@code @SneakyThrows} to handle checked exceptions.
     * Any {@link IOException} during section writing is wrapped in a {@link RuntimeException}.
     * 
     * <p><strong>Performance:</strong> Section writing is performed in parallel using parallel streams
     * to optimize processing time for multiple topics.
     *
     * @throws RuntimeException if newsletter creation fails due to search, planning, or writing errors
     */
    @SneakyThrows
    @Scheduled(cron = "0 0 1 * * SUN")
    public void createNewsletter() {
        TavilySearch searchQuery = new TavilySearch("AI agent workflows project ideas");
        searchQuery.setTimeRange(TimeRange.week);
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

    /**
     * Creates a configured {@link TavilySearch} instance for a specific topic.
     * <p>
     * The search is configured with:
     * <ul>
     *   <li>Time range: Past month ({@link TimeRange#month})</li>
     *   <li>Include raw content: Text only ({@link RawContent#text})</li>
     * </ul>
     * 
     * <p>This configuration ensures comprehensive coverage for topic research
     * while focusing on textual content suitable for newsletter writing.
     *
     * @param topic the topic to search for
     * @return configured TavilySearch instance ready for execution
     */
    private TavilySearch createTopicSearch(String topic) {
        TavilySearch tavilySearch = new TavilySearch(topic);
        tavilySearch.setTimeRange(TimeRange.month);
        tavilySearch.setIncludeRawContent(RawContent.text);
        return tavilySearch;
    }

    /**
     * Performs a web search for a specific topic and returns the results wrapped in a Query record.
     * <p>
     * This method creates a topic-specific search configuration and executes it via {@link TavilyService}.
     * The search results are then paired with the original topic for downstream processing.
     *
     * @param topic the topic to search for
     * @return Query record containing the search response and original topic
     * @throws RuntimeException if the search operation fails due to JSON processing errors
     */
    private Query searchForTopic(String topic) {
        TavilySearch topicSearch = createTopicSearch(topic);
        try {
            return new Query(tavilyService.search(topicSearch), topic);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to search for topic: " + topic, e);
        }
    }

    /**
     * Data transfer object that pairs a Tavily search response with its corresponding topic.
     * <p>
     * This record is used to maintain the association between search results and topics
     * during the parallel processing pipeline in the newsletter creation workflow.
     *
     * @param tavilyResponse the search response containing relevant web results for the topic
     * @param topic the original topic string that was searched for
     */
    public record Query(TavilyResponse tavilyResponse, String topic) {
    }

}
