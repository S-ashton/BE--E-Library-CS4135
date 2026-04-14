package com.elibrary.recommendation_service.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FileStorageService {

    private final ObjectMapper mapper = new ObjectMapper();

    public <T> void save(String filename, T data) {
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
