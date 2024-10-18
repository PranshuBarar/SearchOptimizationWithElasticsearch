package com.optimization_component.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimization_component.enums.ComparisonOperator;
import com.optimization_component.enums.LogicalOperator;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
public class Filter {
    private String field;
    private Object value;
    private ComparisonOperator comparisonOperator;
    private LogicalOperator logicalOperator;

    public Filter(String field, Object value, ComparisonOperator comparisonOperator, LogicalOperator logicalOperator) {
        this.field = field;
        this.value = value;
        this.comparisonOperator = comparisonOperator;
        this.logicalOperator = logicalOperator;
    }
}

