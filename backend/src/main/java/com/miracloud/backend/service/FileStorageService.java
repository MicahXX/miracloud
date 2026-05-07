package com.miracloud.backend.service;

import com.miracloud.backend.dto.FileInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private final Path rootLocation;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

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

    public List<FileInfoDTO> listFilesWithInfo(Long userId) throws IOException {
        Path userDir = rootLocation.resolve(String.valueOf(userId));
        if (!Files.exists(userDir)) return List.of();

        try (Stream<Path> stream = Files.list(userDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .sorted((a, b) -> a.getFileName().toString()
                            .compareToIgnoreCase(b.getFileName().toString()))
                    .map(path -> {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(
                                    path, BasicFileAttributes.class);
                            String date = DATE_FMT.format(attrs.creationTime().toInstant());
                            return new FileInfoDTO(
                                    path.getFileName().toString(),
                                    attrs.size(),
                                    date
                            );
                        } catch (IOException e) {
                            return new FileInfoDTO(
                                    path.getFileName().toString(), 0L, "—");
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    public void rename(Long userId, String oldName, String newName) throws IOException {
        String cleanOld = StringUtils.cleanPath(oldName);
        String cleanNew = StringUtils.cleanPath(newName);

        if (cleanNew.isBlank() || cleanNew.contains(".."))
            throw new IOException("Invalid filename: " + newName);

        Path userDir = rootLocation.resolve(String.valueOf(userId));
        Path source  = userDir.resolve(cleanOld);
        Path target  = userDir.resolve(cleanNew);

        if (!Files.exists(source))
            throw new FileNotFoundException("File not found: " + oldName);
        if (Files.exists(target))
            throw new IOException("A file named '" + newName + "' already exists");

        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    }

    public void delete(Long userId, String filename) throws IOException {
        Path file = rootLocation
                .resolve(String.valueOf(userId))
                .resolve(StringUtils.cleanPath(filename));
        Files.deleteIfExists(file);
    }
}