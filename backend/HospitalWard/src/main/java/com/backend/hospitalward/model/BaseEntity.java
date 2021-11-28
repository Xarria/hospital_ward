package com.backend.hospitalward.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Data
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Version
    @Column(name = "version", nullable = false)
    long version;
}
