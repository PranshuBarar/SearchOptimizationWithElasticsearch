package com.searchoptimizationv2.search_optimization_application_v2.DTOs;

import com.searchoptimizationv2.search_optimization_application_v2.ENUM.Status;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class UserDTO {
    private String username;
    private String email;
    private String password;
    private int groupId;
    private int age;

    @Enumerated(EnumType.STRING)
    private Status status;
}
