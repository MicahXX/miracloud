package com.miracloud.backend.controller;

import com.miracloud.backend.dto.FileInfoDTO;
import com.miracloud.backend.model.User;
import com.miracloud.backend.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<FileInfoDTO>> listFiles(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String path) {
        try {
            return ResponseEntity.ok(fileStorageService.listFilesWithInfo(user.getId(), path));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String path) {
        try {
            fileStorageService.store(user.getId(), file, path);
            return ResponseEntity.ok("Uploaded: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal User user,
            @PathVariable String filename) {
        try {
            Resource resource = fileStorageService.loadAsResource(user.getId(), filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<String> delete(
            @AuthenticationPrincipal User user,
            @PathVariable String filename) {
        try {
            fileStorageService.delete(user.getId(), filename);
            return ResponseEntity.ok("Deleted: " + filename);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
        }
    }

    @PutMapping("/{filename}/rename")
    public ResponseEntity<String> rename(
            @AuthenticationPrincipal User user,
            @PathVariable String filename,
            @RequestBody RenameRequest body) {
        try {
            fileStorageService.rename(user.getId(), filename, body.newName());
            return ResponseEntity.ok("Renamed to: " + body.newName());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Rename failed: " + e.getMessage());
        }
    }

    @PutMapping("/{filename}/move")
    public ResponseEntity<String> move(
            @AuthenticationPrincipal User user,
            @PathVariable String filename,
            @RequestBody MoveRequest body) {
        try {
            fileStorageService.move(user.getId(), filename, body.targetFolder());
            return ResponseEntity.ok("Moved");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Move failed: " + e.getMessage());
        }
    }

    @PostMapping("/folder")
    public ResponseEntity<String> createFolder(
            @AuthenticationPrincipal User user,
            @RequestBody FolderRequest body) {
        try {
            fileStorageService.createFolder(user.getId(), body.name());
            return ResponseEntity.ok("Created: " + body.name());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    public record RenameRequest(String newName) {}
    public record MoveRequest(String targetFolder) {}
    public record FolderRequest(String name) {}
}