package com.waduclay.newsletteragentspringai.agent.planning;


import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@Component
public class TopicFormatConverter implements StructuredOutputConverter<TopicFormat> {
    private final BeanOutputConverter<TopicFormat> outputConverter = new BeanOutputConverter<>(TopicFormat.class);

    @Override
    public String getFormat() {
        return """
                Respond with a VALID JSON OBJECT containing:
                                - "title": a string (the newsletter title)
                                - "topics": an array of strings (comma-separated topics)
                
                            Example format:
                                {"title": "Weekly Tech Digest", "topics": ["AI", "Blockchain", "Cybersecurity"]}
                
                            CRITICAL RULES:
                                1. Output ONLY the JSON object (no additional text, explanations, or markdown)
                                2. If reasoning is needed, wrap it in <think> tags BEFORE the JSON
                                3. Ensure JSON is valid (proper quotes, commas, brackets)
                """;
    }

    @Override
    public TopicFormat convert(String source) {
        String formattedText = source.replaceAll("(?s)<think>.*?</think>", "");
        return outputConverter.convert(formattedText);
    }
}
