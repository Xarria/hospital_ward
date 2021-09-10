package com.backend.hospitalward.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SecondaryTable(name = "Personal_data")
public class Patient {

    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "pesel", nullable = false, length = 11, table = "Personal_data")
    private String pesel;

    @Column(name = "age", nullable = false, table = "Personal_data")
    private int age;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "patient_disease",
            joinColumns = { @JoinColumn(name = "patient") },
            inverseJoinColumns = { @JoinColumn(name = "disease") }
    )
    private List<Disease> diseases;

    @Column(name = "referral_nr", length = 30)
    private String referralNr;

    @Column(name = "referral_date")
    private Timestamp referralDate;

    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "type", nullable = false, referencedColumnName = "id")
    private PatientType patientType;

    @ManyToOne(cascade =  CascadeType.REFRESH)
    @JoinColumn(name = "main_doctor", referencedColumnName = "id")
    private Account mainDoctor;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "covid_status", referencedColumnName = "id")
    private CovidStatus covidStatus;

    @Column(name = "name", nullable = false, length = 20, table = "Personal_data")
    private String name;

    @Column(name = "surname", nullable = false, length = 30, table = "Personal_data")
    private String surname;

    @Column(name = "admission_date")
    private Timestamp admissionDate;

    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @Column(name = "phone_number", nullable = false, length = 11, table = "Personal_data")
    private String phoneNumber;

    @Column(name = "urgent", nullable = false)
    private boolean urgent;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private Account createdBy;

    @Column(name = "creation_date", nullable = false)
    private Timestamp creationDate;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "modified_by", referencedColumnName = "id")
    private Account modifiedBy;

    @Column(name = "modification_date")
    private Timestamp modificationDate;

}
