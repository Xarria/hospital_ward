package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Disease extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "latin_name", nullable = false)
    String latinName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "polish_name", nullable = false)
    String polishName;

    @NotNull
    @Column(name = "catherer_required", nullable = false)
    boolean cathererRequired;

    @NotNull
    @Column(name = "surgery_required", nullable = false)
    boolean surgeryRequired;

    @ManyToMany(mappedBy = "diseases")
    List<Patient> patients;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    Account createdBy;

    @PastOrPresent
    @NotNull
    @Column(name = "creation_date", nullable = false)
    Timestamp creationDate;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "modified_by", referencedColumnName = "id")
    private Account modifiedBy;

    @Column(name = "modification_date")
    private Timestamp modificationDate;

}
