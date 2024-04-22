package com.prox.challenge.model;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelBase {
    public ModelBase() {
        createAt = System.currentTimeMillis();
    }

    @Column(name = "create_at", updatable = false)
    private Long createAt;

    @Column(name = "name")
    private String name;
}
