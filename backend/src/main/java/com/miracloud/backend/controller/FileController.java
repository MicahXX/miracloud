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
    public ResponseEntity<List<FileInfoDTO>> listFiles(@AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(fileStorageService.listFilesWithInfo(user.getId()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Add this new endpoint:
    @PutMapping("/{filename}/rename")
    public ResponseEntity<String> rename(@AuthenticationPrincipal User user,
                                         @PathVariable String filename,
                                         @RequestBody RenameRequest body) {
        try {
            fileStorageService.rename(user.getId(), filename, body.newName());
            return ResponseEntity.ok("Renamed to: " + body.newName());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Rename failed: " + e.getMessage());
        }
    }

    public record RenameRequest(String newName) {}

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@AuthenticationPrincipal User user,
                                         @RequestParam("file") MultipartFile file) {
        try {
            fileStorageService.store(user.getId(), file);
            return ResponseEntity.ok("Uploaded: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal User user,
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
    public ResponseEntity<String> delete(@AuthenticationPrincipal User user,
                                         @PathVariable String filename) {
        try {
            fileStorageService.delete(user.getId(), filename);
            return ResponseEntity.ok("Deleted: " + filename);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
        }
    }
}