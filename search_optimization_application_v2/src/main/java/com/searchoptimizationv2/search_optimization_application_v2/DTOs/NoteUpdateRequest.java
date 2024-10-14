package com.searchoptimizationv2.search_optimization_application_v2.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteUpdateRequest {
    private String noteIdString;
    private String updatedNote;
}
