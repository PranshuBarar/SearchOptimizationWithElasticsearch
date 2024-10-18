package com.searchoptimizationv2.search_optimization_application_v2.searchQueryDTO;

import lombok.Builder;

import java.util.Map;

@Builder
public class searchQueryDTO {
    private String indexName;
    private Map<String, Object> filters;

}
