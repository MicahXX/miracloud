package org.miracloud.frontend.controller;

import org.miracloud.frontend.AppState;

import java.net.URI;
import java.net.http.*;

public class loginController {

    private static final String BASE_URL = "http://localhost:8080/api/auth";

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
                // TODO: navigate to main app view
                return "";
            } else {
                return response.body().replace("ERROR: ", "");
            }

        } catch (Exception e) {
            return "Could not connect to server";
        }
    }

    public void toSignup() {
        AppState.navigateTo("signup");
    }
}