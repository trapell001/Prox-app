package com.prox.challenge.gcoder.service;

import com.prox.challenge.gcoder.controller.PublicController;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheFile {
    @Autowired
    private PublicController publicController;
    @PostConstruct
    public void init(){
        ConfigService.SCHEDULED.scheduleAtFixedRate(publicController::clearCacheFIle, 1, 1, TimeUnit.DAYS);
    }
}
