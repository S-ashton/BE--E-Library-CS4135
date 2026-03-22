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
            mapper.writeValue(new File(filename), data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + filename, e);
        }
    }

    public <T> T load(String filename, Class<T> type) {
        File file = new File(filename);

        if (!file.exists()) {
            return null;
        }

        try {
            return mapper.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }
}
