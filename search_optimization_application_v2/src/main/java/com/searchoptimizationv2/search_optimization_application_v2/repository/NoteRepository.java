package com.searchoptimizationv2.search_optimization_application_v2.repository;


import com.searchoptimizationv2.search_optimization_application_v2.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {

}
