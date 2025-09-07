package com.waduclay.newsletteragentspringai.tavily;

import lombok.Data;

import java.util.List;

@Data
public class TavilyResponse {
    private String query;
    private List<ResultsItem> results;
}
