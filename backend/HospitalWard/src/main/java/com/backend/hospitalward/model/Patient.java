package com.backend.hospitalward.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Patient {
    private long id;
    private long version;
    private String pesel;
    private int age;
    private String referralNr;
    private Timestamp referralDate;
    private String name;
    private String surname;
    private Timestamp admissionDate;
    private boolean confirmed;
    private String phoneNumber;
    private boolean urgent;
    private Timestamp creationDate;
    private Timestamp modificationDate;

    @Id
    @Column(name = "id", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "version", nullable = false)
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Basic
    @Column(name = "pesel", nullable = false, length = 11)
    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }

    @Basic
    @Column(name = "age", nullable = false)
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Basic
    @Column(name = "referral_nr", nullable = true, length = 30)
    public String getReferralNr() {
        return referralNr;
    }

    public void setReferralNr(String referralNr) {
        this.referralNr = referralNr;
    }

    @Basic
    @Column(name = "referral_date", nullable = true)
    public Timestamp getReferralDate() {
        return referralDate;
    }

    public void setReferralDate(Timestamp referralDate) {
        this.referralDate = referralDate;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 20)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "surname", nullable = false, length = 30)
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Basic
    @Column(name = "admission_date", nullable = true)
    public Timestamp getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(Timestamp admissionDate) {
        this.admissionDate = admissionDate;
    }

    @Basic
    @Column(name = "confirmed", nullable = false)
    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Basic
    @Column(name = "phone_number", nullable = false, length = 11)
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Basic
    @Column(name = "urgent", nullable = false)
    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    @Basic
    @Column(name = "creation_date", nullable = false)
    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    @Basic
    @Column(name = "modification_date", nullable = true)
    public Timestamp getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Timestamp modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return id == patient.id && version == patient.version && age == patient.age && confirmed == patient.confirmed && urgent == patient.urgent && Objects.equals(pesel, patient.pesel) && Objects.equals(referralNr, patient.referralNr) && Objects.equals(referralDate, patient.referralDate) && Objects.equals(name, patient.name) && Objects.equals(surname, patient.surname) && Objects.equals(admissionDate, patient.admissionDate) && Objects.equals(phoneNumber, patient.phoneNumber) && Objects.equals(creationDate, patient.creationDate) && Objects.equals(modificationDate, patient.modificationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, pesel, age, referralNr, referralDate, name, surname, admissionDate, confirmed, phoneNumber, urgent, creationDate, modificationDate);
    }
}
