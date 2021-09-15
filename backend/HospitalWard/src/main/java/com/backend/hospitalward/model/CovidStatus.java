package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "covid_status")
public class CovidStatus {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @Column(name = "status", nullable = false, length = 15)
    String status;

}
