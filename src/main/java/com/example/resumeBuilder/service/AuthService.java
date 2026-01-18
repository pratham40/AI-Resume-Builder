package com.example.resumeBuilder.service;

import com.example.resumeBuilder.dto.AuthResponse;
import com.example.resumeBuilder.dto.RegisterRequest;
import com.example.resumeBuilder.entity.User;
import com.example.resumeBuilder.exception.ResourceExistException;
import com.example.resumeBuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;

    public AuthResponse register(RegisterRequest registerRequest) {

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

//        TODO: SEND VERIFICATION EMAIL

        log.info("User registered successfully : {}", user);

        return toResponse(user);



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