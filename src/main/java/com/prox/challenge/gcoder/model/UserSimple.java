package com.prox.challenge.gcoder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "_user")
@Getter
@Setter
public class UserSimple {
    public UserSimple() {
        this.email = "";
        role = Role.user;
    }
    @Id
    private String email;
    @Column(name = "_role")
    private Role role;
}
