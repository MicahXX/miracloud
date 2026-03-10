package com.miracloud.backend.service;

import com.miracloud.backend.model.User;
import com.miracloud.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // SIGNUP
    public String register(String username, String email, String password) {
        if (username == null || username.isBlank())
            return "ERROR: Username is required";
        if (email == null || email.isBlank())
            return "ERROR: Email is required";
        if (password == null || password.isBlank())
            return "ERROR: Password is required";
        if (password.length() < 6)
            return "ERROR: Password must be at least 6 characters";
        if (userRepo.existsByEmail(email))
            return "ERROR: Email already registered";
        if (!email.contains("@") || !email.contains(".")) {
            return "ERROR: Email is invalid";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        userRepo.save(user);

        return "OK";
    }

    // LOGIN
    public String login(String email, String password) {
        if (email == null || email.isBlank())
            return "ERROR: Email is required";
        if (password == null || password.isBlank())
            return "ERROR: Password is required";

        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null || !encoder.matches(password, user.getPassword()))
            return "ERROR: Invalid email or password";

        return "OK:" + user.getUsername(); // send back username on success
    }
}