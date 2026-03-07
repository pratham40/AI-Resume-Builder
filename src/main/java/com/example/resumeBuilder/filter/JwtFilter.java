package com.example.resumeBuilder.filter;

import com.example.resumeBuilder.entity.User;
import com.example.resumeBuilder.repository.UserRepository;
import com.example.resumeBuilder.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        String userId = null;
        String jwtToken = null;
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwtToken = authorizationHeader.substring(7);
                userId = jwtUtils.extractUserId(jwtToken);
            }
        } catch (Exception e) {
            log.error("{}do filter interval", e.getMessage());
            throw new RuntimeException("error parsing JWT token"+e.getMessage());
        }
        if (userId!=null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                User user = userRepository.findById(userId);
                if (user!=null) {
                    boolean tokenValid = jwtUtils.validateToken(jwtToken);
                    if (tokenValid) {
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user,null,new ArrayList<>());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            } catch (Exception e) {
                log.error("error in validating token {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        filterChain.doFilter(request, response);
    }
}
