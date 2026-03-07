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
    public ResponseEntity<String> login(@RequestBody Map<String, String> body) {
        String result = authService.login(
                body.get("email"),
                body.get("password")
        );
        return result.startsWith("ERROR")
                ? ResponseEntity.status(401).body(result)
                : ResponseEntity.ok(result);
    }
}