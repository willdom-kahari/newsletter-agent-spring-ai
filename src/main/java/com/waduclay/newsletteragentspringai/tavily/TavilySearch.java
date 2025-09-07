package com.waduclay.newsletteragentspringai.tavily;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TavilySearch {
    private final String query;
    @JsonProperty("time_range")
    private TimeRange timeRange;
    private Topic topic;
    @Builder.Default
    @JsonProperty("max_results")
    private int maxResults = 5;
    @JsonProperty("include_raw_content")
    private RawContent includeRawContent;
    @JsonProperty("include_domains")
    private List<String> includeDomains;


    public TavilySearch(String query) {
        this.query = query;
        this.maxResults = 5;
        this.includeDomains = List.of(
                "github.com",
                "arxiv.org",
                "medium.com",
                "towardsdatascience.com",
                "n8n.io/workflows",
                "n8n.io/ai-agents"
        );
    }

}
