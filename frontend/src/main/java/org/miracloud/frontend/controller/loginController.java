package org.miracloud.frontend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.miracloud.frontend.AppState;

import java.net.URI;
import java.net.http.*;
import java.util.Map;

public class loginController {

    private static final String BASE_URL = "http://localhost:8080/api/auth";
    private static final ObjectMapper mapper = new ObjectMapper();

    public String handleLogin(String email, String password) {
        if (email.isBlank() || password.isBlank())
            return "Please fill in all fields";

        try {
            String json = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    email, password
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, String> body = mapper.readValue(response.body(), Map.class);
                AppState.saveTokens(
                        body.get("accessToken"),
                        body.get("refreshToken"),
                        body.get("username")
                );
                AppState.navigateTo("app");
                return "";
            } else {
                return response.body().replace("ERROR: ", "");
            }

        } catch (Exception e) {
            return "Could not connect to server";
        }
    }

    // called on app startup to silently restore session
    public static boolean tryAutoLogin() {
        try {
            String refreshToken = AppState.getRefreshToken();
            if (refreshToken == null) return false;

            String json = String.format("{\"refreshToken\":\"%s\"}", refreshToken);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/refresh"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, String> body = mapper.readValue(response.body(), Map.class);
                AppState.saveTokens(
                        body.get("accessToken"),
                        AppState.getRefreshToken(),
                        AppState.getUsername()
                );
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void toSignup() {
        AppState.navigateTo("signup");
    }
}