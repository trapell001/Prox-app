package com.prox.challenge.model;

import com.prox.challenge.gcoder.converter.UrlNameConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "_trending")
@Getter
@Setter
public class Trending {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "_video_id")
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
    private String thumb = "";

    @Column(name = "_show")
    private Boolean show = false;

    @JoinColumn(name = "_type")
    @ManyToOne
    private TrendingType trendingType;

    @Column(name = "_top")
    private Integer top = 0;

    @Column(name = "_like")
    private Integer like = 0;

    @Column(name = "_group")
    private String group;

    @JoinColumn(name = "_music")
    @ManyToOne
    private Music music;

}