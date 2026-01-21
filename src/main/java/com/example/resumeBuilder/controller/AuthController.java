package com.example.resumeBuilder.controller;


import com.example.resumeBuilder.dto.AuthResponse;
import com.example.resumeBuilder.dto.RegisterRequest;
import com.example.resumeBuilder.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){

        AuthResponse authResponse=authService.register(request);
        log.info("User registered successfully: {}", authResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);

    }


    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token){
        authService.verifyEmail(token);
        log.info("Email verified for token: {}", token);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","Email verified successfully"));
    }


}