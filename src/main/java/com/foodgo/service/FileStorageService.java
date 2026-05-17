package com.foodgo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    @Value("${foodgo.data-dir:data}")
    private String dataDir;

    private Path basePath;

    @PostConstruct
    public void init() throws IOException {
        basePath = Paths.get(dataDir).toAbsolutePath().normalize();
        Files.createDirectories(basePath);
    }

    public String resolve(String filename) {
        return basePath.resolve(filename).toString();
    }

    public Path getBasePath() {
        return basePath;
    }
}
