package com.prox.challenge.gcoder.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorRequest {
    private long count = 0;
    private long success = 0;
    private long error = 0;
    private long timeAvgLogic = 0;
    final private String time;
    final private long createAt;
    public void plusSuccess(long time){
        success++;
        timeAvgLogic = (timeAvgLogic + time) / success;
    }
    public void plusError(){
        error++;
    }
    public void plusCount(){
        count++;
    }
    public MonitorRequest(String time) {
        createAt = System.currentTimeMillis();
        this.time = time;
    }
}
