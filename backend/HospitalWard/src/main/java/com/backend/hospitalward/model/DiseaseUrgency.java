package com.backend.hospitalward.model;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "disease_urgency")
public class DiseaseUrgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "id", nullable = false)
    private long id;

    @NotBlank
    @Column(name = "urgency", nullable = false, length = 10)
    private String urgency;

}
