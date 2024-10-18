package com.searchoptimizationv2.search_optimization_application_v2.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.searchoptimizationv2.search_optimization_application_v2.ENUM.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID userId;
    private String username;
    private String email;
    private int groupId;
    private int age;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String password;

    //CHILD//
    /*
     * A user can own multiple notes hence the relationship between them will be:
     * User:Note :: One:Many (i.e. @OneToMany)
     * */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Note> notesList = new ArrayList<>();

}
