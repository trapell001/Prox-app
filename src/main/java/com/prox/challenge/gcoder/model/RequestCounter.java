package com.prox.challenge.gcoder.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RequestCounter {
    public RequestCounter() {
        this.time = LocalDate.now().toString();
    }
    private String uri;
    private final String time;
    private Integer count;
    public void plus(){
        count++;
    }
}
