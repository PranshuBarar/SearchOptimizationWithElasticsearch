package com.optimization_component.payload;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payload {
    private List<Filter> filters;
}



