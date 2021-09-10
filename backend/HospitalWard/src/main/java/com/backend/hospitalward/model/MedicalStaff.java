package com.backend.hospitalward.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "medical_staff")
@DiscriminatorValue("MEDIC")
public class MedicalStaff extends AccessLevel {

    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "license_nr", nullable = false, length = 8)
    private String licenseNr;

    @Column(name = "academic_degree", nullable = false)
    private String academicDegree;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "medical_staff_specialization",
            joinColumns = { @JoinColumn(name = "medical_staff") },
            inverseJoinColumns = { @JoinColumn(name = "specialization") }
    )
    private List<Specialization> specializations;
}
