package com.backend.hospitalward.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Data
@Entity
public class Disease {

    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "urgent", nullable = false)
    private boolean urgent;

    @Column(name = "catherer_required", nullable = false)
    private boolean cathererRequired;

    @Column(name = "surgery_required", nullable = false)
    private boolean surgeryRequired;

    @ManyToMany(mappedBy = "diseases")
    private List<Patient> patients;

}
