package com.apibp.events.controller;

import com.apibp.events.exception.DomainException;
import com.apibp.events.exception.ErrorCode;
import com.apibp.events.repository.AppUserRepository;
import com.apibp.events.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;
    private final AppUserRepository users;

    record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> token(@Valid @RequestBody LoginRequest req) {
        var user = users.findByUsername(req.username())
                .filter(u -> encoder.matches(req.password(), u.getPassword()))
                .orElseThrow(() -> new DomainException(
                        ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED,
                        "Invalid username or password"));
        return ResponseEntity.ok(Map.of("token", jwtUtil.generate(user.getUsername())));
    }
}
