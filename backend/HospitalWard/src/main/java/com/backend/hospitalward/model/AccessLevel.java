package com.backend.hospitalward.model;

import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.DiscriminatorFormula;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "access_level")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
public class AccessLevel {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(lombok.AccessLevel.NONE)
    private long id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account", nullable = false, updatable = false, referencedColumnName = "id")
    private Account account;

    @Column(name = "type", nullable = false)
    private String type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "position", nullable = false, updatable = false, referencedColumnName = "id")
    private Position position;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private Account createdBy;

    @Column(name = "creation_date", nullable = false)
    private Timestamp creationDate;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "modified_by", referencedColumnName = "id")
    private Account modifiedBy;

    @Column(name = "modification_date")
    private Timestamp modificationDate;

}
