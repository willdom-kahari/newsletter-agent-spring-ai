package com.waduclay.newsletteragentspringai.agent.writer;


import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:developer.wadu@gmail.com">Willdom Kahari</a>
 */
@Component
public class SectionFormatConverter implements StructuredOutputConverter<String> {

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public String convert(String source) {
        return source.replaceAll("(?s)<think>.*?</think>", "");
    }
}
