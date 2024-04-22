package com.prox.challenge.gcoder.controller;

import com.prox.challenge.gcoder.service.ConfigService;
import com.prox.challenge.gcoder.model.ConfigValue;
import com.prox.challenge.gcoder.model.Cover;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/config")
public class ConfigValueController {
    @Autowired
    private ConfigService configService;

    @GetMapping
    public ResponseEntity<?> findAllConfig(HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(configService.findAll(httpServletRequest));
    }
    @GetMapping("/apply")
    public ResponseEntity<?> applyConfig(HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(new Cover(200, configService.run(httpServletRequest)));
    }
    @GetMapping("/type")
    public ResponseEntity<?> findAllType(){
        return ResponseEntity.ok(new Cover(200, ConfigValue.Type.values()));
    }

    @PostMapping
    public ResponseEntity<?> saveAllConfig(HttpServletRequest httpServletRequest, @RequestBody List<ConfigValue> valueList){
        return ResponseEntity.ok(configService.saveAll(httpServletRequest, valueList));
    }

    @PostMapping("/firebase")
    public ResponseEntity<?> firebaseConfig(HttpServletRequest httpServletRequest, @RequestParam("file") MultipartFile file){
        configService.configFirebase(httpServletRequest, file);
        return ResponseEntity.ok(new Cover(200, "done"));
    }
}
