package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "medical_staff")
@DiscriminatorValue("MEDIC")
public class MedicalStaff extends Account {

    @Id
    @NotNull
    @Column(name = "id", nullable = false)
    long id;

    @NotBlank
    @Size(max = 8, min = 7)
    @Pattern(regexp = "[0-9]{7}[P]?")
    @Column(name = "license_nr", nullable = false, length = 8)
    String licenseNr;

    @NotBlank
    @Column(name = "academic_degree", nullable = false)
    String academicDegree;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "medical_staff_specialization",
            joinColumns = { @JoinColumn(name = "medical_staff") },
            inverseJoinColumns = { @JoinColumn(name = "specialization") }
    )
    List<Specialization> specializations;
}
