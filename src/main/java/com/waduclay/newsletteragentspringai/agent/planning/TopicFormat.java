package com.waduclay.newsletteragentspringai.agent.planning;

import java.util.List;

/**
 * Data structure representing a planned newsletter format with title and topics.
 * <p>
 * This record is used to structure the output from {@link PlanningAgent}, providing
 * a standardized format for AI-generated newsletter planning. It serves as the blueprint
 * for newsletter creation, containing the main title and a list of specific topics
 * that will be researched and written into individual sections.
 * 
 * <p><strong>Usage in Workflow:</strong>
 * <ol>
 *   <li>{@link PlanningAgent} analyzes web search results and generates this structure</li>
 *   <li>{@link com.waduclay.newsletteragentspringai.workflow.WorkflowOrchestrator} uses the topics list for targeted searches</li>
 *   <li>Each topic becomes a section written by {@link com.waduclay.newsletteragentspringai.agent.writer.SectionWriterAgent}</li>
 *   <li>The title is used by {@link com.waduclay.newsletteragentspringai.agent.editor.EditorAgent} for final formatting</li>
 * </ol>
 * 
 * <p><strong>AI Integration:</strong>
 * This record is designed for Spring AI structured output conversion, allowing AI models
 * to generate consistent, parseable newsletter plans that can be automatically processed
 * by the workflow orchestrator.
 *
 * @param title the main title/theme of the newsletter
 * @param topics list of specific topics to be covered in newsletter sections
 * 
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 * @since 1.0.0
 */
public record TopicFormat(
        String title,
        List<String> topics
) {
}
