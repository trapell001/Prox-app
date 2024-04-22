package com.prox.challenge.service;

import com.prox.challenge.controller.TrendingController;
import com.prox.challenge.gcoder.service.ConfigService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ResetCache {
    @Autowired
    private TrendingController trendingController;
    @PostConstruct
    private void init(){
        ConfigService.SCHEDULED.scheduleAtFixedRate(trendingController::resetCache, 1,1, TimeUnit.MINUTES);
    }


}
