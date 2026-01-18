package com.example.resumeBuilder.controller;


import com.example.resumeBuilder.dto.AuthResponse;
import com.example.resumeBuilder.dto.RegisterRequest;
import com.example.resumeBuilder.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){

        AuthResponse authResponse=authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);

    }

}