package com.prox.challenge.gcoder.service;

public class OSService {
    public static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win");
    }
    public static boolean isUbuntu() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("nix") || osName.contains("nux") || osName.contains("mac");
    }
}
