package com.prox.challenge.model;

import com.prox.challenge.gcoder.converter.UrlNameConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "video_type")
@Getter
@Setter
public class TrendingType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "_id")
    private String id;

    @Column(name = "_name")
    private String name;

    @Column(name = "_value")
    private Integer value;

    @Convert(converter = UrlNameConverter.class)
    private String url;

    @Column(name = "_show")
    private Boolean show;

    @Column(name = "_premium")
    private Boolean premium;

    @Column(name = "_top")
    private Integer top;

    @Column(name = "_rank")
    private Integer rank;

    @Column(name = "_banner")
    private String banner;

    @Column(name = "required_version")
    private Integer requiredVersion;

}