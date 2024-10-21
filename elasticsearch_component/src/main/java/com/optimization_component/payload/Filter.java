package com.optimization_component.payload;

import com.optimization_component.enums.Operator;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
    private Operator operator;
    private String field;
    private Object value;
    private List<Filter> filters;
}