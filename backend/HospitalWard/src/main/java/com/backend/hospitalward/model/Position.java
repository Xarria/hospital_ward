package com.backend.hospitalward.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Data
public class Position {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    String name;

}
