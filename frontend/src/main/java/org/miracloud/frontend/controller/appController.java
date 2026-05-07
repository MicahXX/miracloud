package org.miracloud.frontend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.miracloud.frontend.AppState;

import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class appController {

    private static final String BASE_URL = "http://localhost:8080/api/files";
    private static final ObjectMapper mapper = new ObjectMapper();

    public List<String> listFiles() throws Exception {
        HttpResponse<String> response = authorizedGet(BASE_URL);
        if (response.statusCode() == 200)
            return mapper.readValue(response.body(), List.class);
        if (response.statusCode() == 401 && loginController.tryAutoLogin())
            return listFiles(); // retry once after token refresh
        throw new Exception("Failed to load files");
    }

    public void uploadFile(Path filePath) throws Exception {
        String boundary = "----MiraCloudBoundary" + System.currentTimeMillis();
        byte[] fileBytes = Files.readAllBytes(filePath);
        String fileName = filePath.getFileName().toString();

        String multipartHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: application/octet-stream\r\n\r\n";

        byte[] prefix = multipartHeader.getBytes();
        byte[] suffix = ("\r\n--" + boundary + "--\r\n").getBytes();
        byte[] fullBody = new byte[prefix.length + fileBytes.length + suffix.length];
        System.arraycopy(prefix, 0, fullBody, 0, prefix.length);
        System.arraycopy(fileBytes, 0, fullBody, prefix.length, fileBytes.length);
        System.arraycopy(suffix, 0, fullBody, prefix.length + fileBytes.length, suffix.length);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/upload"))
                .header("Authorization", "Bearer " + AppState.getAccessToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fullBody))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new Exception("Upload failed: " + response.body());
    }

    public void deleteFile(String filename) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + filename))
                .header("Authorization", "Bearer " + AppState.getAccessToken())
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new Exception("Delete failed: " + response.body());
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
}