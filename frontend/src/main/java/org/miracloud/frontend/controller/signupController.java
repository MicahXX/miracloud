package org.miracloud.frontend.controller;
import javafx.application.Platform;
import org.miracloud.frontend.AppState;
import java.net.URI;
import java.net.http.*;

public class signupController {
    private static final String BASE_URL = "https://miracloud-api.rafilaos.vip/api/auth";
    public String handleSignup(String email, String username, String password, boolean acceptedPolicy) {
        if (!acceptedPolicy)
            return "Please accept the privacy policy";
        if (username.isBlank() || email.isBlank() || password.isBlank())
            return "Please fill in all fields";
        try {
            String json = String.format(
                    "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                    username, email, password
            );
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Platform.runLater(this::toLogin);
                return "";
            } else {
                return response.body().replace("ERROR: ", "");
            }
        } catch (Exception e) {
            return "Could not connect to server";
        }
    }

    public void toLogin() {
        AppState.navigateTo("login");
    }
}