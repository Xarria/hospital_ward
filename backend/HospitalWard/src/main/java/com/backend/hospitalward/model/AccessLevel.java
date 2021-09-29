package com.backend.hospitalward.model;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "access_level")
public class AccessLevel implements GrantedAuthority {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE)
    long id;

    @Size(max = 21)
    @Pattern(regexp = "[A-Z]+")
    @Column(name = "name", nullable = false)
    String name;

    @Override
    public String getAuthority() {
        return name;
    }

}
