package com.prox.challenge.gcoder.model;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class DFileFolder extends DFile{
    private List<DFile> files;
    public DFileFolder() {
        this.type = "folder";
        this.files = new ArrayList<>();
    }

    public DFileFolder(String name,String path, List<DFile> files) {
        super(name, "folder", path);
        this.files = files;
    }

    public DFileFolder(List<DFile> files) {
        this.files = files;
    }
}
