package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Specialization {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    String name;

    @Getter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "specializations")
    List<MedicalStaff> medicalStaffList;

}
