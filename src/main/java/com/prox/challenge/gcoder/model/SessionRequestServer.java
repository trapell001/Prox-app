package com.prox.challenge.gcoder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "session_request_server")
@Getter
@Setter
public class SessionRequestServer {
    public SessionRequestServer() {
        time = System.currentTimeMillis();
    }

    @Id
    @Column(name = "_id")
    private String sessionId;
    @Column(name = "type")
    private String type;
    @Column(name = "_begin")
    private Long begin;
    @Column(name = "_end")
    private Long end;
    @Column(name = "_create_at_time", updatable = false)
    private final Long time;

    public LocalDateTime getLocalDateTime (){
        Instant instant = Instant.ofEpochMilli(time);
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}
