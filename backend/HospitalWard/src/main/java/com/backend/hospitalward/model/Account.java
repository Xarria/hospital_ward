package com.backend.hospitalward.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@SuperBuilder
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
@DiscriminatorValue("OFFICE")
public class Account extends BaseEntity implements UserDetails {

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(name = "login", nullable = false, length = 50)
    String login;

    @Size(max = 255)
    @ToString.Exclude
    @Column(name = "password")
    String password;

    @Size(max = 6, min = 5)
    @Column(name = "type", insertable = false)
    String type;

    @NotNull
    @ManyToOne(cascade = CascadeType.REFRESH, optional = false)
    @JoinColumn(name = "access_level", referencedColumnName = "id")
    AccessLevel accessLevel;

    @NotBlank
    @Size(max = 20)
    //@Pattern(regexp = "[A-Z][a-z]+")
    @Column(name = "name", nullable = false, length = 20)
    String name;

    @NotBlank
    @Size(max = 30)
    // @Pattern(regexp = "[A-Z][a-z]+")
    @Column(name = "surname", nullable = false, length = 30)
    String surname;

    @NotBlank
    @Size(max = 50)
    @Email
    @Column(name = "email", nullable = false, length = 50, unique = true)
    String email;

    @NotNull
    @Column(name = "active", nullable = false)
    boolean active = true;

    @NotNull
    @Column(name = "confirmed", nullable = false)
    boolean confirmed = false;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    Account createdBy;

    @PastOrPresent
    @NotNull
    @Column(name = "creation_date", nullable = false)
    Timestamp creationDate = Timestamp.from(Instant.now());

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "modified_by", referencedColumnName = "id")
    Account modifiedBy;

    @PastOrPresent
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
    public String getPassword() {
        return password;
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
        return confirmed;
    }
}
