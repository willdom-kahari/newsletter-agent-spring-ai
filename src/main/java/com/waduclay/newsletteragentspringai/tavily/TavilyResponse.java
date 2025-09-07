package com.waduclay.newsletteragentspringai.tavily;

import java.util.List;
import lombok.Data;

@Data
public class TavilyResponse{
	private String query;
	private List<ResultsItem> results;
}
