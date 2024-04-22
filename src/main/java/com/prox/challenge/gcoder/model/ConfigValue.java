package com.prox.challenge.gcoder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "config_value")
@Getter
@Setter
public class ConfigValue {
    @Id
    @Column(name = "_key", nullable = false)
    private String key;
    @Column(name = "_value", nullable = false, columnDefinition = "LONGTEXT")
    private String value;
    @Column(name = "_description", nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String description;
    @Column(name = "_type")
    private Type type;

    public enum Type{
        upload, backup, security, monitor, main, client
    }
}
