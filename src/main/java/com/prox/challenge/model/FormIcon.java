package com.prox.challenge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "form_icon")
@Getter
@Setter
public class FormIcon extends ModelBase {

    public FormIcon() {
        createAt = System.currentTimeMillis();
    }

    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JoinColumn(name = "_alphabet")
    @OneToMany
    private List<Alphabet> alphabets;

    @Column(name = "create_at", updatable = false)
    private Long createAt;

    @Column(name = "name")
    private String name;

    @Column(name = "required_version")
    private Integer requiredVersion;
}
