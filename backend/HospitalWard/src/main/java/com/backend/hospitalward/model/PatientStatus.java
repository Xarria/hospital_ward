package com.backend.hospitalward.model;


import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@SuperBuilder
@Table(name = "patient_status")
public class PatientStatus {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @Column(name = "status", nullable = false, length = 21, unique = true)
    String name;
}
