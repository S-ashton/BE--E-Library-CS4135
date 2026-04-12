package com.elibrary.recommendation_service.storage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @Test
    void savesAndLoadsJson() {
        FileStorageService storage = new FileStorageService();

        List<String> data = List.of("A", "B", "C");

        storage.save("test.json", data);

        List loaded = storage.load("test.json", List.class);

        assertEquals(data, loaded);
    }
}

