package com.prox.challenge.gcoder.controller;

import com.prox.challenge.gcoder.service.H2DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/setting/edit/h2")
public class DatabaseH2Controller {
    @Autowired
    private H2DatabaseService h2DatabaseService;

    @GetMapping("/change-column-type")
    public ResponseEntity<?> changeColumnType(@RequestParam("table") String table,
                                              @RequestParam("column") String column,
                                              @RequestParam("type") H2DatabaseService.Type type) {
        h2DatabaseService.changeTypeColumn(table, column, type);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/table")
    public ResponseEntity<?> changeColumnType() {
        return ResponseEntity.ok(h2DatabaseService.getTableNames());
    }

    @GetMapping("/column")
    public ResponseEntity<?> getObjectTableColumn(@RequestParam("table") String table) {
        return ResponseEntity.ok(h2DatabaseService.getTableColumns(table));
    }
}
