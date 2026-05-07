package org.miracloud.frontend.views;

public record FileEntry(String name, long sizeBytes, String lastModified) {

    public boolean isFolder() { return sizeBytes == -1L; }

    // checks for what so like MB, GB, etc.
    public String formattedSize() {
        if (isFolder()) return "";
        if (sizeBytes < 1024)               return sizeBytes + " B";
        if (sizeBytes < 1024 * 1024)        return String.format("%.1f KB", sizeBytes / 1024.0);
        if (sizeBytes < 1024 * 1024 * 1024) return String.format("%.1f MB", sizeBytes / (1024.0 * 1024));
        return String.format("%.1f GB", sizeBytes / (1024.0 * 1024 * 1024));
    }
}