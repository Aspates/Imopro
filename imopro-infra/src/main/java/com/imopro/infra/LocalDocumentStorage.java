package com.imopro.infra;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class LocalDocumentStorage {
    public Path importFile(Path source) {
        try {
            Path attachments = DataPaths.attachmentsDir();
            String fileName = source.getFileName().toString();
            Path target = attachments.resolve(UUID.randomUUID() + "_" + fileName);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return attachments.relativize(target);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to import file", e);
        }
    }

    public Path absolutePath(String relativePath) {
        return DataPaths.attachmentsDir().resolve(relativePath);
    }

    public void open(String relativePath) {
        try {
            Path absolute = absolutePath(relativePath);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(absolute.toFile());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open file", e);
        }
    }

    public void deleteIfExists(String relativePath) {
        try {
            Files.deleteIfExists(absolutePath(relativePath));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to delete file", e);
        }
    }
}
