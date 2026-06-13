package com.miracloud.backend.service;

import com.miracloud.backend.model.RefreshToken;
import com.miracloud.backend.model.User;
import com.miracloud.backend.repository.RefreshTokenRepository;
import com.miracloud.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    public AuthService(UserRepository userRepo,
                       RefreshTokenRepository refreshTokenRepo,
                       JwtService jwtService,
                       FileStorageService fileStorageService) {
        this.userRepo = userRepo;
        this.refreshTokenRepo = refreshTokenRepo;
        this.jwtService = jwtService;
        this.fileStorageService = fileStorageService;
    }

    public String register(String username, String email, String password) {
        if (username == null || username.isBlank()) return "ERROR: Username is required";
        if (email == null || email.isBlank())       return "ERROR: Email is required";
        if (password == null || password.isBlank()) return "ERROR: Password is required";
        if (password.length() < 6)                  return "ERROR: Password must be at least 6 characters";
        if (!email.contains("@") || !email.contains(".")) return "ERROR: Email is invalid";
        if (userRepo.existsByEmail(email))           return "ERROR: Email already registered";

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));

        User saved = userRepo.save(user);
        System.out.println(">>> User saved with ID: " + saved.getId());

        try {
            fileStorageService.initUserStorage(saved.getId());
            System.out.println(">>> Storage folder created for user: " + saved.getId());
        } catch (IOException e) {
            System.out.println(">>> FAILED to create storage: " + e.getMessage());
            throw new RuntimeException("Failed to create storage for user", e);
        }

        return "OK";
    }

    public Map<String, String> login(String email, String password) {
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null || !encoder.matches(password, user.getPassword()))
            return Map.of("error", "Invalid email or password");

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());

        refreshTokenRepo.deleteByUser(user);
        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user);
        refresh.setToken(UUID.randomUUID().toString());
        refresh.setExpiryDate(Instant.now().plusSeconds(60 * 60 * 24 * refreshExpirationDays));
        refreshTokenRepo.save(refresh);

        return Map.of(
                "accessToken",  accessToken,
                "refreshToken", refresh.getToken(),
                "username",     user.getUsername()
        );
    }

    public Map<String, String> refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepo.findByToken(refreshToken).orElse(null);
        if (stored == null)
            return Map.of("error", "Refresh token not found");
        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepo.delete(stored);
            return Map.of("error", "Refresh token expired");
        }

        String newAccessToken = jwtService.generateAccessToken(
                stored.getUser().getId(),
                stored.getUser().getEmail()
        );
        return Map.of("accessToken", newAccessToken);
    }

    public void logout(String refreshToken) {
        refreshTokenRepo.findByToken(refreshToken)
                .ifPresent(refreshTokenRepo::delete);
    }
}