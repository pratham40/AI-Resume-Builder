package com.example.resumeBuilder.controller;


import com.example.resumeBuilder.dto.AuthResponse;
import com.example.resumeBuilder.dto.LoginRequest;
import com.example.resumeBuilder.dto.RegisterRequest;
import com.example.resumeBuilder.service.AuthService;
import com.example.resumeBuilder.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    private final FileUploadService fileUploadService;

    @PostMapping(
            value = "/register",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> register(@Valid @ModelAttribute RegisterRequest request,
                                      @RequestPart(value = "profileImageUrl") MultipartFile image
    ) throws IOException {
        log.info("Received request to register with data: {}", request.toString());
        Map uploadResult = fileUploadService.uploadSingleImage(image);
        String imageUrl = uploadResult.get("secure_url").toString();
        AuthResponse authResponse=authService.register(request,imageUrl);
        log.info("User registered successfully: {}", authResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);

    }


    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token){
        authService.verifyEmail(token);
        log.info("Email verified for token: {}", token);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","Email verified successfully"));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestPart("image") MultipartFile file) throws  IOException {
        log.info("Received image upload request: {}", file.getOriginalFilename());
        Map map = fileUploadService.uploadSingleImage(file);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("SECURE_URL",map.get("secure_url"),"PUBLIC_ID",map.get("public_id"),"message","Image uploaded successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest){
        try {
            AuthResponse response=authService.login(loginRequest);
            log.info("User logged in successfully: {}", response);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Error while logging in: {} for user : {}", e.getMessage(), loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",e.getMessage()));
        }
    }


}