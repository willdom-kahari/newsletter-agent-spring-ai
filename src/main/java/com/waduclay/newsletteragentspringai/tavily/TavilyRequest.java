package com.waduclay.newsletteragentspringai.tavily;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TavilyRequest{
	private String endDate;
	private String country;
	private Boolean includeFavicon;
	private final String query;
	private List<String> includeDomains;
	private Boolean includeImages;
	private String searchDepth;
	private String timeRange;
	private String topic;
    @Builder.Default
	private int maxResults = 5;
	private List<String> excludeDomains;
	private Boolean includeImageDescriptions;
	private String includeRawContent;
	private String startDate;
}
