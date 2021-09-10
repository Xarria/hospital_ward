package com.backend.hospitalward.model;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name = "covid_status")
public class CovidStatus {

    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "status", nullable = false, length = 15)
    private String status;

}
