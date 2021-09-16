package com.backend.hospitalward.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
public class Account implements UserDetails {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE)
    long id;

    @Version
    @Column(name = "version", nullable = false)
    long version;

    @Column(name = "login", nullable = false, length = 50)
    String login;

    @Column(name = "password", nullable = false, length = 50)
    String password;

    @Column(name = "type", nullable = false)
    String type;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "access_level", referencedColumnName = "id")
    AccessLevel accessLevel;

    @Column(name = "name", nullable = false, length = 20)
    String name;

    @Column(name = "surname", nullable = false, length = 30)
    String surname;

    @Column(name = "active", nullable = false)
    boolean active;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    Account createdBy;

    @Column(name = "creation_date", nullable = false)
    Timestamp creationDate;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "modified_by", referencedColumnName = "id")
    Account modifiedBy;

    @Column(name = "modification_date")
    Timestamp modificationDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(accessLevel);
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
