package com.backend.hospitalward.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity
public class Disease {
    private long id;
    private String name;
    private boolean urgent;
    private boolean cathererRequired;
    private boolean surgeryRequired;

    @Id
    @Column(name = "id", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    @Column(name = "catherer_required", nullable = false)
    public boolean isCathererRequired() {
        return cathererRequired;
    }

    public void setCathererRequired(boolean cathererRequired) {
        this.cathererRequired = cathererRequired;
    }

    @Basic
    @Column(name = "surgery_required", nullable = false)
    public boolean isSurgeryRequired() {
        return surgeryRequired;
    }

    public void setSurgeryRequired(boolean surgeryRequired) {
        this.surgeryRequired = surgeryRequired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disease disease = (Disease) o;
        return id == disease.id && urgent == disease.urgent && cathererRequired == disease.cathererRequired && surgeryRequired == disease.surgeryRequired && Objects.equals(name, disease.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, urgent, cathererRequired, surgeryRequired);
    }
}
