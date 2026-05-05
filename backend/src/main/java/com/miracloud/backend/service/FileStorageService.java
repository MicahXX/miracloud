package com.miracloud.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${storage.path}") String storagePath) {
        this.rootLocation = Paths.get(storagePath);
        System.out.println(">>> FileStorageService initialized. Root: " + rootLocation.toAbsolutePath());
    }

    public void initUserStorage(Long userId) throws IOException {
        Path userDir = rootLocation.resolve(String.valueOf(userId));
        Files.createDirectories(userDir);
        System.out.println(">>> Created storage folder: " + userDir.toAbsolutePath());
    }

    public void store(Long userId, MultipartFile file) throws IOException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        if (filename.isBlank() || filename.contains(".."))
            throw new IOException("Invalid filename: " + filename);

        Path userDir = rootLocation.resolve(String.valueOf(userId));
        Files.copy(file.getInputStream(), userDir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
    }

    public Resource loadAsResource(Long userId, String filename) throws IOException {
        Path file = rootLocation
                .resolve(String.valueOf(userId))
                .resolve(StringUtils.cleanPath(filename));

        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() && resource.isReadable()) return resource;

        throw new FileNotFoundException("File not found: " + filename);
    }

    public List<String> listFiles(Long userId) throws IOException {
        Path userDir = rootLocation.resolve(String.valueOf(userId));
        try (Stream<Path> stream = Files.list(userDir)) {
            return stream
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public void delete(Long userId, String filename) throws IOException {
        Path file = rootLocation
                .resolve(String.valueOf(userId))
                .resolve(StringUtils.cleanPath(filename));
        Files.deleteIfExists(file);
    }
}