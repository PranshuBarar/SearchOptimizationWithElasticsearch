package com.searchoptimizationv2.search_optimization_application_v2.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserDTO {
    private String username;
    private String email;
    private String password;
}
