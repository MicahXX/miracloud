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
    }

    private Path resolveUserPath(Long userId, String subPath) throws IOException {
        Path userDir = rootLocation.resolve(String.valueOf(userId));
        Path resolved = subPath == null || subPath.isBlank()
                ? userDir
                : userDir.resolve(subPath).normalize();

        if (!resolved.startsWith(userDir))
            throw new IOException("Access denied: path escapes user directory");

        return resolved;
    }

    public void store(Long userId, MultipartFile file, String subPath) throws IOException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (filename.isBlank() || filename.contains(".."))
            throw new IOException("Invalid filename: " + filename);

        Path dir = resolveUserPath(userId, subPath);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
    }

    public void store(Long userId, MultipartFile file) throws IOException {
        store(userId, file, null);
    }

    public Resource loadAsResource(Long userId, String filename) throws IOException {
        Path userDir = rootLocation.resolve(String.valueOf(userId));
        Path file = userDir.resolve(StringUtils.cleanPath(filename)).normalize();

        if (!file.startsWith(userDir))
            throw new IOException("Access denied");

        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() && resource.isReadable()) return resource;

        throw new FileNotFoundException("File not found: " + filename);
    }

    public List<FileInfoDTO> listFilesWithInfo(Long userId, String subPath) throws IOException {
        Path dir = resolveUserPath(userId, subPath);
        if (!Files.exists(dir)) return List.of();

        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .sorted((a, b) -> {
                        boolean aDir = Files.isDirectory(a);
                        boolean bDir = Files.isDirectory(b);
                        if (aDir != bDir) return aDir ? -1 : 1;
                        return a.getFileName().toString()
                                .compareToIgnoreCase(b.getFileName().toString());
                    })
                    .map(path -> {
                        try {
                            boolean isDir = Files.isDirectory(path);
                            BasicFileAttributes attrs = Files.readAttributes(
                                    path, BasicFileAttributes.class);
                            String date = DATE_FMT.format(attrs.creationTime().toInstant());
                            return new FileInfoDTO(
                                    path.getFileName().toString(),
                                    isDir ? -1L : attrs.size(),
                                    date
                            );
                        } catch (IOException e) {
                            return new FileInfoDTO(path.getFileName().toString(), 0L, "—");
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    public List<FileInfoDTO> listFilesWithInfo(Long userId) throws IOException {
        return listFilesWithInfo(userId, null);
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

    public void rename(Long userId, String oldName, String newName) throws IOException {
        String cleanOld = StringUtils.cleanPath(oldName);
        String cleanNew = StringUtils.cleanPath(newName);

        if (cleanNew.isBlank() || cleanNew.contains(".."))
            throw new IOException("Invalid filename: " + newName);

        Path userDir = rootLocation.resolve(String.valueOf(userId));
        Path source = userDir.resolve(cleanOld);
        Path target = userDir.resolve(cleanNew);

        if (!source.startsWith(userDir) || !target.startsWith(userDir))
            throw new IOException("Access denied");
        if (!Files.exists(source))
            throw new FileNotFoundException("File not found: " + oldName);
        if (Files.exists(target))
            throw new IOException("A file named '" + newName + "' already exists");

        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    }

    public void move(Long userId, String filename, String targetFolder) throws IOException {
        Path userDir = rootLocation.resolve(String.valueOf(userId));
        Path source = userDir.resolve(StringUtils.cleanPath(filename)).normalize();
        Path destDir = targetFolder == null || targetFolder.isBlank()
                ? userDir
                : userDir.resolve(StringUtils.cleanPath(targetFolder)).normalize();
        Path dest = destDir.resolve(source.getFileName());

        if (!source.startsWith(userDir) || !dest.startsWith(userDir))
            throw new IOException("Access denied");
        if (!Files.exists(source))
            throw new FileNotFoundException("File not found: " + filename);
        if (!Files.isDirectory(destDir))
            throw new IOException("Target is not a folder: " + targetFolder);
        if (Files.exists(dest))
            throw new IOException("A file with that name already exists in the target folder");

        Files.move(source, dest, StandardCopyOption.ATOMIC_MOVE);
    }

    public void delete(Long userId, String filename) throws IOException {
        Path userDir = rootLocation.resolve(String.valueOf(userId));
        Path file = userDir.resolve(StringUtils.cleanPath(filename)).normalize();
        if (!file.startsWith(userDir))
            throw new IOException("Access denied");
        Files.deleteIfExists(file);
    }

    public void createFolder(Long userId, String folderName) throws IOException {
        String clean = StringUtils.cleanPath(folderName);
        if (clean.isBlank() || clean.contains(".."))
            throw new IOException("Invalid folder name: " + folderName);

        Path folder = rootLocation.resolve(String.valueOf(userId)).resolve(clean);
        if (Files.exists(folder))
            throw new IOException("Folder already exists: " + folderName);

        Files.createDirectories(folder);
    }
}