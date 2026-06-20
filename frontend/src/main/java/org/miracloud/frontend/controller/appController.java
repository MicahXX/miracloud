package org.miracloud.frontend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.miracloud.frontend.AppState;
import org.miracloud.frontend.views.FileEntry;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class appController {

    private static final String BASE_URL = "https://miracloud-api.rafilaos.vip/api/files";
    private static final ObjectMapper mapper = new ObjectMapper();

    public List<FileEntry> listFiles() throws Exception {
        return listFiles(null);
    }

    public List<FileEntry> listFiles(String subPath) throws Exception {
        String url = BASE_URL;
        if (subPath != null && !subPath.isBlank())
            url += "?path=" + URLEncoder.encode(subPath, StandardCharsets.UTF_8);

        HttpResponse<String> response = authorizedGet(url);
        if (response.statusCode() == 401 && loginController.tryAutoLogin())
            return listFiles(subPath);
        if (response.statusCode() != 200)
            throw new Exception("Failed to load files");

        JsonNode root = mapper.readTree(response.body());
        List<FileEntry> entries = new ArrayList<>();
        for (JsonNode node : root) {
            String name  = node.has("name")       ? node.get("name").asText()       : node.asText();
            long size    = node.has("sizeBytes")   ? node.get("sizeBytes").asLong()  : 0L;
            String date  = node.has("uploadedAt")  ? node.get("uploadedAt").asText() : "—";
            entries.add(new FileEntry(name, size, date));
        }
        return entries;
    }

    public void uploadFile(Path filePath) throws Exception {
        uploadFile(filePath, null);
    }

    public void uploadFile(Path filePath, String subPath) throws Exception {
        String boundary = "----MiraCloudBoundary" + System.currentTimeMillis();
        byte[] fileBytes = Files.readAllBytes(filePath);
        String fileName = filePath.getFileName().toString();

        String mimeType = detectMimeType(filePath);

        // Raw filename in Content-Disposition (servers parse this more reliably)
        String multipartHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: " + mimeType + "\r\n\r\n";

        // Encoded filename only for the URL path
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        // Always use explicit UTF-8 for boundary strings
        byte[] prefix   = multipartHeader.getBytes(StandardCharsets.UTF_8);
        byte[] suffix   = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] fullBody = new byte[prefix.length + fileBytes.length + suffix.length];
        System.arraycopy(prefix,    0, fullBody, 0,                                prefix.length);
        System.arraycopy(fileBytes, 0, fullBody, prefix.length,                    fileBytes.length);
        System.arraycopy(suffix,    0, fullBody, prefix.length + fileBytes.length, suffix.length);

        String url = BASE_URL + "/upload";
        if (subPath != null && !subPath.isBlank())
            url += "?path=" + URLEncoder.encode(subPath, StandardCharsets.UTF_8);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AppState.getAccessToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fullBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new Exception("Upload failed: " + response.body());
    }

    public void deleteFile(String filename) throws Exception {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + encoded))
                .header("Authorization", "Bearer " + AppState.getAccessToken())
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new Exception("Delete failed: " + response.body());
    }

    public void renameFile(String oldName, String newName) throws Exception {
        String encoded = URLEncoder.encode(oldName, StandardCharsets.UTF_8).replace("+", "%20");
        String json = String.format("{\"newName\":\"%s\"}", newName);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + encoded + "/rename"))
                .header("Authorization", "Bearer " + AppState.getAccessToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new Exception("Rename failed: " + response.body());
    }

    public void moveFile(String filename, String targetFolder) throws Exception {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        String json = String.format("{\"targetFolder\":\"%s\"}", targetFolder);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + encoded + "/move"))
                .header("Authorization", "Bearer " + AppState.getAccessToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new Exception("Move failed: " + response.body());
    }

    public void createFolder(String folderName) throws Exception {
        String json = String.format("{\"name\":\"%s\"}", folderName);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/folder"))
                .header("Authorization", "Bearer " + AppState.getAccessToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new Exception("Folder creation failed: " + response.body());
    }

    public File downloadToTemp(String filename) throws Exception {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        HttpResponse<byte[]> response = authorizedGetBytes(BASE_URL + "/download/" + encoded);
        if (response.statusCode() != 200)
            throw new Exception("Download failed: " + response.statusCode());

        Path tmp = Files.createTempFile("miracloud_", "_" + filename);
        Files.write(tmp, response.body());
        tmp.toFile().deleteOnExit();
        return tmp.toFile();
    }

    public void logout() throws Exception {
        String refreshToken = AppState.getRefreshToken();
        if (refreshToken != null) {
            String json = String.format("{\"refreshToken\":\"%s\"}", refreshToken);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://miracloud-api.rafilaos.vip/api/auth/logout"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        AppState.clearTokens();
    }

    private static String detectMimeType(Path filePath) {
        String name = filePath.getFileName().toString().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".gif"))  return "image/gif";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".pdf"))  return "application/pdf";
        if (name.endsWith(".zip"))  return "application/zip";
        if (name.endsWith(".tar"))  return "application/x-tar";
        if (name.endsWith(".gz"))   return "application/gzip";
        if (name.endsWith(".mp4"))  return "video/mp4";
        if (name.endsWith(".mkv"))  return "video/x-matroska";
        if (name.endsWith(".mp3"))  return "audio/mpeg";
        if (name.endsWith(".wav"))  return "audio/wav";
        if (name.endsWith(".txt"))  return "text/plain";
        if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html";
        if (name.endsWith(".json")) return "application/json";
        if (name.endsWith(".xml"))  return "application/xml";
        if (name.endsWith(".csv"))  return "text/csv";
        if (name.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (name.endsWith(".xls"))  return "application/vnd.ms-excel";
        if (name.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (name.endsWith(".doc"))  return "application/msword";
        if (name.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (name.endsWith(".ppt"))  return "application/vnd.ms-powerpoint";

        // Fall back to OS probe, then generic binary
        try {
            String probed = Files.probeContentType(filePath);
            if (probed != null) return probed;
        } catch (Exception ignored) {}

        return "application/octet-stream";
    }

    private HttpResponse<String> authorizedGet(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AppState.getAccessToken())
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<byte[]> authorizedGetBytes(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + AppState.getAccessToken())
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}