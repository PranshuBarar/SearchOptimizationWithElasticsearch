package com.searchoptimizationv2.search_optimization_application_v2.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class NoteDTO {
    private String note;
    private String userIdString;
}
