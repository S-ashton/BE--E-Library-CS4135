package com.elibrary.recommendation_service.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

@Component
public class FileStorageService {

    private static final Path ALLOWED_BASE = Path.of("data").toAbsolutePath().normalize();

    private final ObjectMapper mapper = new ObjectMapper();

    private void validatePath(String filename) {
        try {
            Path resolved = Path.of(filename).toAbsolutePath().normalize();
            if (!resolved.startsWith(ALLOWED_BASE)) {
                throw new SecurityException("Access to path outside data directory is not allowed: " + filename);
            }
        } catch (InvalidPathException e) {
            throw new SecurityException("Invalid file path: " + filename, e);
        }
    }

    public <T> void save(String filename, T data) {
        validatePath(filename);
        try {
            File file = new File(filename);
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            mapper.writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + filename, e);
        }
    }

    public <T> T load(String filename, Class<T> type) {
        validatePath(filename);
        File file = new File(filename);

        if (!file.exists() || file.length() == 0) {
            return null;
        }

        try {
            return mapper.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }
}
