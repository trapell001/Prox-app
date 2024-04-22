package com.prox.challenge.gcoder.model;

import com.prox.challenge.gcoder.service.ConfigService;
import com.prox.challenge.gcoder.service.FileService;
import com.prox.challenge.gcoder.service.MonitorService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Log4j2
public class RequestCounterCover {

    public RequestCounterCover() {
        this.date = LocalDate.now().toString();
        this.map = new HashMap<>();

    }

    @Getter
    private final String date;
    private final Map<String, RequestCounter> map;
    public synchronized RequestCounterCover add(String uri){
        if(!map.containsKey(uri)){
            RequestCounter requestCounter = new RequestCounter();
            requestCounter.setUri(uri);
            requestCounter.setCount(0);
            map.put(uri, requestCounter);
        }
        map.get(uri).plus();
        return this;
    }
    public synchronized void writeFile() {
        try{
            String path = MonitorService.PATH_WRITE_LOG_MONITOR + "request";
            Path p = Paths.get(path);
            if(!Files.exists(p)){
                Files.createDirectories(p);
            }
            String content = ConfigService.GSON.toJson(map.values());
            FileService.writeFile(path + "/" + date + ".json", content);
        }catch (Exception e){
            log.error("requestCounterCover.writeFile : " + e.getMessage());
        }
    }
}
