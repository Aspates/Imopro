package com.imopro.infra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DataPaths {
    private DataPaths() {
    }

    public static Path dataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                return Paths.get(appData, "Imopro");
            }
        }
        if (os.contains("mac")) {
            String home = System.getProperty("user.home");
            return Paths.get(home, "Library", "Application Support", "Imopro");
        }
        String home = System.getProperty("user.home");
        return Paths.get(home, ".local", "share", "imopro");
    }

    public static Path ensureDataDir() {
        Path dir = dataDir();
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create data directory: " + dir, e);
        }
        return dir;
    }

    public static Path databasePath() {
        return ensureDataDir().resolve("imopro.sqlite");
    }

    public static Path attachmentsDir() {
        Path dir = ensureDataDir().resolve("attachments");
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create attachments directory: " + dir, e);
        }
        return dir;
    }
}
