package com.backend.hospitalward.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.time.Instant;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
public class Url {

    @Id
    @Column(name = "id", nullable = false)
    long id;

    @Version
    @Column(name = "version", nullable = false)
    long version;

    @Column(name = "action_type", nullable = false, length = 10)
    String actionType;

    @NotBlank
    @Size(min = 5, max = 5)
    @Column(name = "code_director", nullable = false, length = 5)
    String codeDirector;

    @NotBlank
    @Size(min = 5, max = 5)
    @Column(name = "code_employee", nullable = false, length = 5)
    String codeEmployee;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "account_director", referencedColumnName = "id")
    Account accountDirector;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "account_employee", referencedColumnName = "id")
    Account accountEmployee;

    @Column(name = "expiration_date", nullable = false)
    Timestamp expirationDate;

    @Column(name = "creation_date", nullable = false)
    Timestamp creationDate = Timestamp.from(Instant.now());

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    Account createdBy;

}
