package com.miracloud.backend.controller;

import com.miracloud.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> body) {
        String result = authService.register(
                body.get("username"),
                body.get("email"),
                body.get("password")
        );
        return result.startsWith("ERROR")
                ? ResponseEntity.badRequest().body(result)
                : ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        Map<String, String> result = authService.login(
                body.get("email"),
                body.get("password")
        );
        return result.containsKey("error")
                ? ResponseEntity.status(401).body(result)
                : ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        Map<String, String> result = authService.refresh(body.get("refreshToken"));
        return result.containsKey("error")
                ? ResponseEntity.status(401).body(result)
                : ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> body) {
        authService.logout(body.get("refreshToken"));
        return ResponseEntity.ok("Logged out");
    }
}