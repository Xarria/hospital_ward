package com.backend.hospitalward.model;

import lombok.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Queue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Future
    @NotNull
    @Column(name = "date", nullable = false, unique = true)
    private Date date;

    @OneToMany(mappedBy = "queue", fetch = FetchType.EAGER)
    List<Patient> patientsWaiting;

    @OneToMany(mappedBy = "queue", fetch = FetchType.EAGER)
    List<Patient> patientsConfirmed;

    @NotNull
    @Column(name = "locked", nullable = false)
    private boolean locked;

    public List<Patient> getAllPatients(){
        List<Patient> allPatients = new ArrayList<>();
        allPatients.addAll(patientsWaiting);
        allPatients.addAll(patientsConfirmed);
        return allPatients;
    }

}
