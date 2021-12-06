package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "covid_status")
public class CovidStatus {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @NotBlank
    @Size(max = 15)
    @Pattern(regexp = "[A-Z]+")
    @Column(name = "status", nullable = false, length = 15)
    String status;

}
