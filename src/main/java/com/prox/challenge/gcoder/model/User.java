package com.prox.challenge.gcoder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "_user")
@Getter
@Setter
public class User {
    public User() {
        this.email = "";
        this.password = "";
        role = Role.user;
    }

    @Id
    private String email;
    private String password;
    @Column(name = "_role")
    private Role role;
    @Transient
    private String newPass;
}