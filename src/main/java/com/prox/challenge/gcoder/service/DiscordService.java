package com.prox.challenge.gcoder.service;

import com.prox.challenge.gcoder.model.ConfigValue;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class DiscordService {
    private String urlWebHook;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Gson gson;
    @Autowired
    private ConfigService configService;
    @PostConstruct
    private void init(){
        configService.addConfigActions("url discord webhook",
                "https://discord.com/api/webhooks/1093383522506506361/mPCgx8J5l9V5Cd8g4vlN_Cu4CtN145AYUXDTTYtD2WBUNqQaD0h_BMFbUbEkrshpVQzl",
                "Discord of server", ConfigValue.Type.main,
                s -> urlWebHook = s);
    }
    public void sendDiscord(String content) {
        String json = gson.toJson(Map.of("content", content));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        restTemplate.exchange(urlWebHook, HttpMethod.POST, request, String.class);
    }
}
