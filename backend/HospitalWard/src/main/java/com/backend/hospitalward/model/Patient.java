package com.backend.hospitalward.model;

import com.backend.hospitalward.model.common.PatientTypeName;
import lombok.AccessLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@SecondaryTables({
        @SecondaryTable(name="Personal_data"),
        @SecondaryTable(name="Patient_position")
})
public class Patient extends BaseEntity {

    @NotBlank
    @Size(min = 11, max = 11)
    @Column(name = "pesel", nullable = false, length = 11, table = "Personal_data")
    String pesel;

    @NotBlank
    @Column(name = "age", nullable = false, length = 3, table = "Personal_data")
    String age;

    @NotBlank
    @Size(max = 1)
    @Pattern(regexp = "[MF]")
    @Column(name = "sex", nullable = false, table = "Personal_data")
    String sex;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "patient_disease",
            joinColumns = {@JoinColumn(name = "patient")},
            inverseJoinColumns = {@JoinColumn(name = "disease")}
    )
    List<Disease> diseases;

    @Column(name = "referral_nr", length = 30)
    String referralNr;

    @Column(name = "referral_date")
    LocalDate referralDate;

    //queue

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "queue", nullable = false, table = "Patient_position")
    Queue queue;

    @Column(name = "position", table = "Patient_position")
    Integer positionInQueue;

    //

    @NotNull
    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "type", nullable = false, referencedColumnName = "id")
    PatientType patientType;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "main_doctor", referencedColumnName = "id")
    Account mainDoctor;

    @NotNull
    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "covid_status", referencedColumnName = "id")
    CovidStatus covidStatus;

    @NotBlank
    @Size(max = 20)
    @Column(name = "name", nullable = false, length = 20, table = "Personal_data")
    String name;

    @NotBlank
    @Size(max = 30)
    @Column(name = "surname", nullable = false, length = 30, table = "Personal_data")
    String surname;

    @NotNull
    @Column(name = "admission_date", nullable = false)
    LocalDate admissionDate;

    @NotNull
    @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "status", referencedColumnName = "id")
    PatientStatus status;

    @Column(name = "phone_number", length = 11, table = "Personal_data")
    String phoneNumber;

    @Column(name = "email_address", length = 50, table = "Personal_data")
    String emailAddress;

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

    public String findPatientType() {
        if (urgent) {
            return PatientTypeName.URGENT.name();
        }
        if (isUnder9MonthsOlds()) {
            return PatientTypeName.INTENSIVE_SUPERVISION.name();
        }
        if (isUnder6YearsOld()) {
            return PatientTypeName.UNDER_6.name();
        }
        if (sex.equals("M")) {
            return PatientTypeName.BOY.name();
        } else {
            return PatientTypeName.GIRL.name();
        }
    }

    private boolean isUnder9MonthsOlds() {
        return age.endsWith("M") && Integer.parseInt(age.substring(0, age.length() - 1)) <= 9;
    }

    private boolean isUnder6YearsOld() {
        return age.endsWith("M") || age.endsWith("Y") && Integer.parseInt(age.substring(0, age.length() - 1)) <= 6;
    }

}
