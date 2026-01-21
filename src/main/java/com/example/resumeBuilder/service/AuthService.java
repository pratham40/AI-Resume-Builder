package com.example.resumeBuilder.service;

import com.example.resumeBuilder.dto.AuthResponse;
import com.example.resumeBuilder.dto.RegisterRequest;
import com.example.resumeBuilder.entity.User;
import com.example.resumeBuilder.exception.ResourceExistException;
import com.example.resumeBuilder.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {
    @Value("${app.base.url:http://localhost:8080}")
    private String appUrl;

    private final EmailService emailService;
    private final UserRepository userRepository;

    public AuthService(EmailService emailService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public AuthResponse register(RegisterRequest registerRequest) {

        try {
            log.info("Register request received : {}", registerRequest);


            if (userRepository.existsByEmail(registerRequest.getEmail())){
                throw new ResourceExistException("User with email "+registerRequest.getEmail()+" already exists");
            }

            User user=User.builder()
                    .name(registerRequest.getName())
                    .email(registerRequest.getEmail())
                    .password(registerRequest.getPassword())
                    .profileImageUrl(registerRequest.getProfileImageUrl())
                    .subscriptionPlan("basic")
                    .emailVerified(false)
                    .verificationToken(UUID.randomUUID().toString())
                    .verificationExpiry(LocalDateTime.now().plusMinutes(15))
                    .build();


            userRepository.save(user);


            sendVerificationEmail(user);

            log.info("User registered successfully : {}", user);

            return toResponse(user);
        } catch (ResourceExistException e) {
            log.error("Registration failed for {}: {}", registerRequest.getEmail(), e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }


    }

    private void sendVerificationEmail(User user) {
        try{
            String link = appUrl+"resume/api/auth/verify-email?token="+user.getVerificationToken();
            String htmlContent = "<p>Dear " + user.getName() + ",</p>"
                    + "<p>Thank you for registering. Please click the link below to verify your email address:</p>"
                    + "<a href=\"" + link + "\">Verify Email</a>"
                    + "<p>Paste this link on the browser </p>"
                    + link
                    + "<p>This link will expire in 15 minutes.</p>"
                    + "<p>If you did not register, please ignore this email.</p>"
                    + "<p>Best regards,<br/>Resume Builder Team</p>";


            emailService.sendHtmlEmail(user.getEmail(), "Email Verification", htmlContent);
            log.info("Verification email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send verification email to "+user.getEmail()+": "+e.getMessage());
        }
    }

    public void verifyEmail(String token) {

        try{
            Optional<User> user = userRepository.findByVerificationToken(token);
            if (user.isEmpty()) {
                throw new RuntimeException("Invalid verification token");
            }

            if (user.get().getVerificationExpiry().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification token has expired Please request a new one");
            }

            String tokenUser = user.get().getVerificationToken();
            if (!tokenUser.equals(token)) {
                throw new RuntimeException("Invalid verification token");
            }

            user.get().setEmailVerified(true);
            user.get().setVerificationToken(null);
            user.get().setVerificationExpiry(null);

            userRepository.save(user.get());
        } catch (Exception e) {
            log.error("Email verification failed for token {}: {}", token, e.getMessage());
            throw new RuntimeException("Email verification failed: " + e.getMessage());
        }
    }


    private AuthResponse toResponse(User user){
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .subscriptionPlan(user.getSubscriptionPlan())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}