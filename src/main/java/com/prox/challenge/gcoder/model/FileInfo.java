package com.prox.challenge.gcoder.model;

public record FileInfo(String fileName, String path, long size, boolean file, long lastModified, String type) {
}
