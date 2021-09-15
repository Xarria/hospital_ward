package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@SecondaryTable(name = "Personal_data")
public class Patient {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @Version
    @Column(name = "version", nullable = false)
    long version;

    @Column(name = "pesel", nullable = false, length = 11, table = "Personal_data")
    String pesel;

    @Column(name = "age", nullable = false, table = "Personal_data")
    int age;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "patient_disease",
            joinColumns = { @JoinColumn(name = "patient") },
            inverseJoinColumns = { @JoinColumn(name = "disease") }
    )
    List<Disease> diseases;

    @Column(name = "referral_nr", length = 30)
    String referralNr;

    @Column(name = "referral_date")
    Timestamp referralDate;

    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "type", nullable = false, referencedColumnName = "id")
    PatientType patientType;

    @ManyToOne(cascade =  CascadeType.REFRESH)
    @JoinColumn(name = "main_doctor", referencedColumnName = "id")
    Account mainDoctor;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "covid_status", referencedColumnName = "id")
    CovidStatus covidStatus;

    @Column(name = "name", nullable = false, length = 20, table = "Personal_data")
    String name;

    @Column(name = "surname", nullable = false, length = 30, table = "Personal_data")
    String surname;

    @Column(name = "admission_date")
    Timestamp admissionDate;

    @Column(name = "confirmed", nullable = false)
    boolean confirmed;

    @Column(name = "phone_number", nullable = false, length = 11, table = "Personal_data")
    String phoneNumber;

    @Column(name = "urgent", nullable = false)
    boolean urgent;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    Account createdBy;

    @Column(name = "creation_date", nullable = false)
    Timestamp creationDate;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "modified_by", referencedColumnName = "id")
    private Account modifiedBy;

    @Column(name = "modification_date")
    private Timestamp modificationDate;

}
