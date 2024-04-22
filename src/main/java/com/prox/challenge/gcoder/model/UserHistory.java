package com.prox.challenge.gcoder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "_user_history")
@Getter
@Setter
public class UserHistory {
    public UserHistory() {
        this.time = System.currentTimeMillis();
    }
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "_email")
    private String email;
    @Column(name = "_uri", columnDefinition = "LONGTEXT")
    private String uri;
    @Column(name = "_method")
    private String method;
    @Column(name = "_time")
    private Long time;
}
