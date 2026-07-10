package com.medicare.controller;

import com.medicare.dto.JwtResponse;
import com.medicare.dto.LoginRequest;
import com.medicare.dto.UserRegistrationDto;
import com.medicare.model.User;
import com.medicare.security.JwtTokenProvider;
import com.medicare.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User registered = userService.registerPatient(registrationDto);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("email", registered.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            User userDetails = (User) authentication.getPrincipal();

            // Set HTTP-Only Cookie for Thymeleaf Web View Authentication
            boolean isSecure = request.isSecure() || "https".equals(request.getHeader("X-Forwarded-Proto"));
            org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt_token", jwt)
                    .httpOnly(true)
                    .secure(isSecure)
                    .path("/")
                    .maxAge(86400) // 24 hours
                    .sameSite("Lax")
                    .build();
            response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getEmail(),
                    userDetails.getFullName(),
                    userDetails.getRole()
            ));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad credentials. Invalid email or password.");
            return ResponseEntity.status(401).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // Clear HTTP-only authentication cookie
        boolean isSecure = request.isSecure() || "https".equals(request.getHeader("X-Forwarded-Proto"));
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, String> result = new HashMap<>();
        result.put("message", "Logged out successfully");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        // In a real app, send email with reset link. Here we simulate it.
        System.out.println("Forgot password requested for: " + email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "If an account exists with this email, a reset password link has been sent.");
        return ResponseEntity.ok(response);
    }
}
