package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Table(name = "patient_type")
public class PatientType {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @Column(name = "name", nullable = false, length = 21, unique = true)
    String name;

}
