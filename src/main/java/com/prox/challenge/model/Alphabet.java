package com.prox.challenge.model;

import com.prox.challenge.gcoder.converter.UrlNameConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "_alphabet")
@Getter
@Setter
public class Alphabet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "_id")
    private Long id;

    @Column(name = "_name")
    private String name;

    @Column(name = "_url")
    @Convert(converter = UrlNameConverter.class)
    private String url;

    public Alphabet setUrl(String url) {
        this.url = url;
        return this;
    }
}
