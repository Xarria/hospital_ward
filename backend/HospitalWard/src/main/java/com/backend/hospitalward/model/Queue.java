package com.backend.hospitalward.model;

import lombok.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
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
    @Column(name = "date", nullable = false)
    private Date date = Date.valueOf(LocalDate.now());

    @OneToMany(mappedBy = "queue", fetch = FetchType.EAGER)
    List<Patient> patients;

    @NotNull
    @Column(name = "locked", nullable = false)
    private boolean locked;

}
