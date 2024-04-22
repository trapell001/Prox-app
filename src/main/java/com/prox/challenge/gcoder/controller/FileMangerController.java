package com.prox.challenge.gcoder.controller;

import com.prox.challenge.gcoder.model.Cover;
import com.prox.challenge.gcoder.service.DFileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@CrossOrigin("*")
@RequestMapping("/file-manager")
public class FileMangerController {

    @Autowired
    private DFileService dFileService;


    @GetMapping("/scan")
    public ResponseEntity<?> scan(
            HttpServletRequest httpServletRequest,
            @RequestParam("source_path") Optional<String> sourcePath
    ) {
        return ResponseEntity.ok(dFileService.scanDirectorySecurityAdmin(httpServletRequest, sourcePath.orElseThrow(() -> new RuntimeException("source_path requirement"))));
    }

    @GetMapping("/copy")
    public ResponseEntity<?> copy(
            HttpServletRequest httpServletRequest,
            @RequestParam("source_path") Optional<String> sourcePath,
            @RequestParam("destination_path") Optional<String> destinationPath
    ) {
        dFileService.actionCopySecurityAdmin(
                httpServletRequest,
                sourcePath.orElseThrow(() -> new RuntimeException("source_path requirement")),
                destinationPath.orElseThrow(() -> new RuntimeException("destination_path requirement"))
        );
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    @GetMapping("/cut")
    public ResponseEntity<?> cut(
            HttpServletRequest httpServletRequest,
            @RequestParam("source_path") Optional<String> sourcePath,
            @RequestParam("destination_path") Optional<String> destinationPath
    ) {
        dFileService.actionCutSecurityAdmin(
                httpServletRequest,
                sourcePath.orElseThrow(() -> new RuntimeException("source_path requirement")),
                destinationPath.orElseThrow(() -> new RuntimeException("destination_path requirement"))
        );
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    @GetMapping("/delete")
    public ResponseEntity<?> delete(
            HttpServletRequest httpServletRequest,
            @RequestParam("source_path") Optional<String> sourcePath
    ) {
        dFileService.actionDeleteSecurityAdmin(
                httpServletRequest,
                sourcePath.orElseThrow(() -> new RuntimeException("source_path requirement")));
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    @GetMapping("/rename")
    public ResponseEntity<?> rename(
            HttpServletRequest httpServletRequest,
            @RequestParam("source_path") Optional<String> sourcePath,
            @RequestParam("new_name") Optional<String> newName
    ) {
        dFileService.actionRenameSecurityAdmin(
                httpServletRequest,
                sourcePath.orElseThrow(() -> new RuntimeException("source_path requirement")),
                newName.orElseThrow(() -> new RuntimeException("new_name requirement"))
                );
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    @GetMapping("/static")
    public ResponseEntity<Resource> serveFile(
            HttpServletRequest httpServletRequest,
            @RequestParam("source_path") Optional<String> filename
    ){
            return ResponseEntity.ok().body(dFileService.getFile(httpServletRequest, filename.orElseThrow(() -> new RuntimeException("source_path requirement"))));
    }
}
