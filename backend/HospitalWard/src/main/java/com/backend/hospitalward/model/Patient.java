package com.backend.hospitalward.model;

import lombok.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.PackagePrivate;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@SecondaryTable(name = "Personal_data")
public class Patient extends BaseEntity{

    @NotBlank
    @Size(min = 11, max = 11)
    @Column(name = "pesel", nullable = false, length = 11, table = "Personal_data")
    String pesel;

    @NotBlank
    @Size(max = 3, min = 2)
    @Pattern(regexp = "[1-9][0-9]*[MY]")
    @Column(name = "age", nullable = false, length = 3, table = "Personal_data")
    String age;

    @NotBlank
    @Size(max = 1)
    @Pattern(regexp = "[MFU]")
    @Column(name = "sex", nullable = false, table = "Personal_data")
    String sex;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "patient_disease",
            joinColumns = { @JoinColumn(name = "patient") },
            inverseJoinColumns = { @JoinColumn(name = "disease") }
    )
    List<Disease> diseases;

    //TODO dopytać o walidację
    @Size(min = 1)
    @Column(name = "referral_nr", length = 30)
    String referralNr;

    @Column(name = "referral_date")
    Timestamp referralDate;

    @NotNull
    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "type", nullable = false, referencedColumnName = "id")
    PatientType patientType;

    @ManyToOne(cascade =  CascadeType.REFRESH)
    @JoinColumn(name = "main_doctor", referencedColumnName = "id")
    Account mainDoctor;

    @NotNull
    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "covid_status", referencedColumnName = "id")
    CovidStatus covidStatus;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "[A-Z][a-z]+")
    @Column(name = "name", nullable = false, length = 20, table = "Personal_data")
    String name;

    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "[A-Z][a-z]+")
    @Column(name = "surname", nullable = false, length = 30, table = "Personal_data")
    String surname;

    @Column(name = "admission_date")
    Timestamp admissionDate;

    @NotNull
    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "status", referencedColumnName = "id")
    PatientStatus status;

    @NotBlank
    @Pattern(regexp = "[0-9]{9,11}")
    @Column(name = "phone_number", nullable = false, length = 11, table = "Personal_data")
    String phoneNumber;

    @NotNull
    @Column(name = "urgent", nullable = false)
    boolean urgent;

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
