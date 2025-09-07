package com.waduclay.newsletteragentspringai.tavily;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResultsItem {
    private BigDecimal score;
    @JsonProperty("raw_content")
    private String rawContent;
    private String title;
    private String url;
    private String content;
}
