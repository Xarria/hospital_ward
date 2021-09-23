package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Table(name = "patient_type")
public class PatientType {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @NotBlank
    @Size(max = 21)
    @Column(name = "name", nullable = false, length = 21, unique = true)
    String name;

}
