package com.optimization_component.payload;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class Payload {
    private List<Filter> filters;

    public Payload(List<Filter> filters) {
        this.filters = filters;
    }

    public Payload() {
    }
}
