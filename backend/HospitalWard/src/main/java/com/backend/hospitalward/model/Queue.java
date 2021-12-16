package com.backend.hospitalward.model;

import com.backend.hospitalward.model.common.PatientStatusName;
import lombok.AccessLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private LocalDate date;

    @OneToMany(mappedBy = "queue", cascade = CascadeType.REFRESH)
    List<Patient> patients;

    @NotNull
    @Column(name = "locked", nullable = false)
    private boolean locked;

    public List<Patient> getWaitingPatients(){
        return patients.stream()
                .filter(p -> !p.getStatus().getName().equals(PatientStatusName.CONFIRMED_TWICE.name()))
                .collect(Collectors.toList());
    }

    public List<Patient> getConfirmedPatients(){
        return patients.stream()
                .filter(p -> p.getStatus().getName().equals(PatientStatusName.CONFIRMED_TWICE.name()))
                .collect(Collectors.toList());
    }

}
