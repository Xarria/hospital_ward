package com.backend.hospitalward.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account {

    @Id
    @Column(name = "id", nullable = false)
    private long id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "login", nullable = false, length = 50)
    private String login;

    @Column(name = "password", nullable = false, length = 50)
    private String password;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "surname", nullable = false, length = 30)
    private String surname;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "creation_date", nullable = false)
    private Timestamp creationDate;

    @Column(name = "modification_date")
    private Timestamp modificationDate;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id && version == account.version && active == account.active && Objects.equals(login, account.login) && Objects.equals(password, account.password) && Objects.equals(name, account.name) && Objects.equals(surname, account.surname) && Objects.equals(creationDate, account.creationDate) && Objects.equals(modificationDate, account.modificationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, login, password, name, surname, active, creationDate, modificationDate);
    }
}
