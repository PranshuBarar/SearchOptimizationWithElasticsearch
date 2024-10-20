package com.optimization_component.payload;

import com.optimization_component.enums.Operator;
import com.optimization_component.payload.interfaces.Filter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CompositeFilter implements Filter {
    private Operator operator;
    private List<LeafFilter> leafFilters;
}
