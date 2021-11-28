package com.backend.hospitalward.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@SuperBuilder
@Table(name = "access_level")
public class AccessLevel implements GrantedAuthority {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE)
    long id;

    @Size(max = 21)
    @Column(name = "name", nullable = false)
    String name;

    @Override
    public String getAuthority() {
        return name;
    }

}
