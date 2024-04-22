package com.prox.challenge.gcoder.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.prox.challenge.gcoder.service.ConfigService;
import com.prox.challenge.gcoder.model.Cover;
import com.prox.challenge.gcoder.model.FormLoginEmail;
import com.prox.challenge.gcoder.model.User;
import com.prox.challenge.gcoder.service.*;
import com.prox.challenge.gcoder.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@CrossOrigin("*")
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private DFileService dFileService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private HandlerService handlerService;
    @Autowired
    private BackupService backupService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        if (user.getEmail() == null || user.getPassword() == null || user.getEmail().isEmpty() || user.getPassword().isEmpty())
            throw new RuntimeException("Email or Password cant empty!");
        return ResponseEntity.ok(new Cover(200, securityService.login(user)));
    }
    @PostMapping("/login-email")
    public ResponseEntity<?> loginEmail(@RequestBody FormLoginEmail formLoginEmail) throws FirebaseAuthException {
        return ResponseEntity.ok(new Cover(200, securityService.loginFirebase(formLoginEmail.tokenId())));
    }

    @GetMapping("/check-error")
    public ResponseEntity<?> checkError(){
        return ResponseEntity.ok(handlerService.getError());
    }
    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        String fullPath = ConfigService.PATH_PUBLIC_FILE + filename;
        Path file = Paths.get(fullPath);
        Resource resource = dFileService.getFile(fullPath);
        return ResponseEntity.ok()
                .header("Content-Type", Files.probeContentType(file))
                .body(resource);
    }
    @GetMapping("/file/fast/{filename:.+}")
    @Cacheable(value = "file", key = "#filename")
    public ResponseEntity<Resource> serveFileFast(@PathVariable String filename) throws IOException {
       return serveFile(filename);
    }
    @GetMapping("/file/clear")
    @CacheEvict(value = "file", beforeInvocation = true)
    public void clearCacheFIle(){}

    @GetMapping("/firebase-client")
    public ResponseEntity<?> configFirebase(){
        String fullPath = ConfigService.PATH_CONFIG_FILE + "firebase_client.json";
        return ResponseEntity.ok().body(dFileService.getFile(fullPath));
    }

    //========================= Backup ==============================

    @GetMapping("/backup/mysql")
    public ResponseEntity<Resource> backupMysql(HttpServletRequest httpServletRequest) {
        Resource resource = backupService.createMysqlBackupApi(httpServletRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"backup.sql\"")
                .body(resource);
    }
    @GetMapping("/backup/file")
    public ResponseEntity<Resource> backupFile(HttpServletRequest httpServletRequest) throws IOException {
        Resource resource = backupService.createFileBackupApi(httpServletRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"backup.zip\"")
                .body(resource);
    }

}
