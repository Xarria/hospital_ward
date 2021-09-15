package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Disease {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "urgent", nullable = false)
    boolean urgent;

    @Column(name = "catherer_required", nullable = false)
    boolean cathererRequired;

    @Column(name = "surgery_required", nullable = false)
    boolean surgeryRequired;

    @ManyToMany(mappedBy = "diseases")
    List<Patient> patients;

}
