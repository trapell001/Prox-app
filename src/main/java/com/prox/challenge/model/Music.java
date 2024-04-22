package com.prox.challenge.model;

import com.prox.challenge.gcoder.converter.UrlNameConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "_music")
@Getter
@Setter
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "_music_id")
    private String id;

    @Column(name = "_name")
    private String name = "";

    @Column(name = "_url")
    @Convert(converter = UrlNameConverter.class)
    private String url = "";

    @Column(name = "_description")
    private String description = "";

    @Column(name = "_thumb")
    @Convert(converter = UrlNameConverter.class)
    private String thumb  = "";

    @Column(name = "_show")
    private Boolean show = false;

    @Column(name = "_top")
    private Integer top = 0;

}
