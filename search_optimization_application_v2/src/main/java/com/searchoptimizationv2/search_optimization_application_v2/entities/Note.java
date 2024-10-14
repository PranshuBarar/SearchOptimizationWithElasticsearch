package com.searchoptimizationv2.search_optimization_application_v2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Builder
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "noteId")
    private UUID noteId;

    @Column(name = "note")
    private String note;

    //PARENT//
    /*
    Parent of this Note is User.
    As a user can have multiple notes so the relationship between Note and User is
    Note:User :: Many:One (i.e. @ManyToOne)
    */
    @ManyToOne
    @JoinColumn(name = "note_owner_id")
    @JsonIgnore
    private User owner;

}
