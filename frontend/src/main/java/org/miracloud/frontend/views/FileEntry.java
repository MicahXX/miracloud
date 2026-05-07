package org.miracloud.frontend.views;

public record FileEntry(String name, long sizeBytes, String lastModified) {

    // Used to check if MB etc
    public String formattedSize() {
        if (sizeBytes < 1024)             return sizeBytes + " B";
        if (sizeBytes < 1024 * 1024)      return String.format("%.1f KB", sizeBytes / 1024.0);
        if (sizeBytes < 1024 * 1024 * 1024) return String.format("%.1f MB", sizeBytes / (1024.0 * 1024));
        return String.format("%.1f GB", sizeBytes / (1024.0 * 1024 * 1024));
    }
}