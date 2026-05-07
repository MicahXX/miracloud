package com.miracloud.backend.dto;

// holds the data for the files
public record FileInfoDTO(String name, long sizeBytes, String uploadedAt) {}