package com.searchoptimizationv2.search_optimization_application_v2.repository;

import com.searchoptimizationv2.search_optimization_application_v2.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsername(String username);
}
