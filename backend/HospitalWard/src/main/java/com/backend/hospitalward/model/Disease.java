package com.backend.hospitalward.model;

import lombok.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Disease extends BaseEntity{

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false)
    String name;

    @NotNull
    @Column(name = "urgent", nullable = false)
    boolean urgent;

    @NotNull
    @Column(name = "catherer_required", nullable = false)
    boolean cathererRequired;

    @NotNull
    @Column(name = "surgery_required", nullable = false)
    boolean surgeryRequired;

    @ManyToMany(mappedBy = "diseases")
    List<Patient> patients;

}
