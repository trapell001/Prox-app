package com.prox.challenge.model;

import com.prox.challenge.gcoder.converter.UrlNameConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "_image")
@Getter
@Setter
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "_url")
    @Convert(converter = UrlNameConverter.class)
    private String url;

    @Column(name = "_type")
    private String type;
}
