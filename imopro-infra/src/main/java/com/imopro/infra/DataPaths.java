package com.imopro.infra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

public final class DataPaths {
    private static final String KEY_DATA_DIR = "imopro.data.dir";
    private static final Preferences PREFS = Preferences.userNodeForPackage(DataPaths.class);

    private DataPaths() {
    }

    public static Path dataDir() {
        String configuredPath = PREFS.get(KEY_DATA_DIR, "");
        if (configuredPath != null && !configuredPath.isBlank()) {
            try {
                return Paths.get(configuredPath);
            } catch (Exception ignored) {
                // If the configured path is invalid, fallback to OS default.
            }
        }

        return defaultDataDir();
    }

    public static void setDataDir(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Data directory path cannot be null");
        }
        Path absolute = path.toAbsolutePath().normalize();
        try {
            Files.createDirectories(absolute);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create data directory: " + absolute, e);
        }
        PREFS.put(KEY_DATA_DIR, absolute.toString());
    }

    private static Path defaultDataDir() {
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
