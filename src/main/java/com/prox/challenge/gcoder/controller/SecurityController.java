package com.prox.challenge.gcoder.controller;

import com.prox.challenge.gcoder.model.Cover;
import com.prox.challenge.gcoder.model.User;
import com.prox.challenge.gcoder.model.UserSimple;
import com.prox.challenge.gcoder.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/security")
public class SecurityController {
    @Autowired
    private SecurityService securityService;

    @GetMapping("/user-info")
    public ResponseEntity<?> userInfo(HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(new Cover(200, securityService.userInfo(httpServletRequest)));
    }

    @GetMapping("/user-list")
    public ResponseEntity<?> userList(HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(new Cover(200, securityService.userList(httpServletRequest)));
    }

    @PostMapping("/save-user")
    public ResponseEntity<?> saveUser(HttpServletRequest httpServletRequest,
                                      @RequestBody UserSimple userSimple) {
        return ResponseEntity.ok(new Cover(200, securityService.saveUser(httpServletRequest, userSimple)));
    }

    @PostMapping("/delete-user")
    public ResponseEntity<?> deleteUser(HttpServletRequest httpServletRequest, @RequestParam("email") Optional<String> email) {
        securityService.deleteUser(httpServletRequest, email.orElseThrow(() -> new RuntimeException("param (email) requirement")));
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest httpServletRequest, @RequestBody User user) {
        securityService.changePassword(httpServletRequest, user);
        return ResponseEntity.ok(new Cover(200, "done"));
    }


}
