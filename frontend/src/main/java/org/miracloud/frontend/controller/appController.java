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

    private static final String BASE_URL = "https://miracloud.rafilaos.vip/api/files";
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
            String name     = node.has("name")       ? node.get("name").asText()       : node.asText();
            long size        = node.has("sizeBytes")  ? node.get("sizeBytes").asLong()  : 0L;
            String date      = node.has("uploadedAt") ? node.get("uploadedAt").asText() : "—";
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

        String multipartHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";

        byte[] prefix  = multipartHeader.getBytes();
        byte[] suffix  = ("\r\n--" + boundary + "--\r\n").getBytes();
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
                    .uri(URI.create("http://localhost:8080/api/auth/logout"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        AppState.clearTokens();
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