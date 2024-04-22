package com.prox.challenge.gcoder.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ControllerAdvice
@Log4j2
public class HandlerService {
    private static final Map<String, Integer> error = new ConcurrentHashMap<>();
    public static void addError(String e){
        if(!error.containsKey(e)){
            error.put(e, 1);
        }else {
            int value = error.get(e) + 1;
            error.replace(e, value);
        }
    }
    record CoverError(String detail){}
    public List<String> getError(){
        List<String> result = new ArrayList<>();
        error.forEach((s, integer) -> result.add(s + "(" + integer + ")"));
        error.clear();
        return result;
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> ex(Exception e){
        e.printStackTrace();
        return new ResponseEntity<>(new CoverError(e.getMessage()), HttpStatus.valueOf(500));
    }
}
