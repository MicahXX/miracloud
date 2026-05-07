package com.miracloud.backend.dto;

public record FileInfoDTO(String name, long sizeBytes, String uploadedAt) {
    public boolean isFolder() { return sizeBytes == -1L; }
}