package com.backend.hospitalward.model;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "medical_staff")
public class MedicalStaff {
    private long id;
    private long version;
    private String licenseNr;
    private String academicDegree;
    private String position;
    private Timestamp creationDate;
    private Timestamp modificationDate;
    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "medical_staff_specialization",
            joinColumns = { @JoinColumn(name = "medical_staff") },
            inverseJoinColumns = { @JoinColumn(name = "specialization") }
    )
    private List<Specialization> specializations;

    @Id
    @Column(name = "id", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Version
    @Column(name = "version", nullable = false)
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Basic
    @Column(name = "license_nr", nullable = false, length = 8)
    public String getLicenseNr() {
        return licenseNr;
    }

    public void setLicenseNr(String licenseNr) {
        this.licenseNr = licenseNr;
    }

    @Basic
    @Column(name = "academic_degree", nullable = false, length = 255)
    public String getAcademicDegree() {
        return academicDegree;
    }

    public void setAcademicDegree(String academicDegree) {
        this.academicDegree = academicDegree;
    }

    @Basic
    @Column(name = "position", nullable = false, length = 255)
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Basic
    @Column(name = "creation_date", nullable = false)
    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalStaff that = (MedicalStaff) o;
        return id == that.id && version == that.version && Objects.equals(licenseNr, that.licenseNr) && Objects.equals(academicDegree, that.academicDegree) && Objects.equals(position, that.position) && Objects.equals(creationDate, that.creationDate) && Objects.equals(modificationDate, that.modificationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, licenseNr, academicDegree, position, creationDate, modificationDate);
    }
}
