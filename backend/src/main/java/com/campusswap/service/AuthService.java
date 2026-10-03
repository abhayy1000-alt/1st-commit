package com.campusswap.service;

import com.campusswap.dto.AuthResponse;
import com.campusswap.dto.LoginRequest;
import com.campusswap.dto.SignupRequest;
import com.campusswap.dto.UserResponse;
import com.campusswap.exception.ApiException;
import com.campusswap.model.User;
import com.campusswap.repository.UserRepository;
import com.campusswap.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = normalize(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("An account with this email already exists.");
        }
        if (userRepository.existsByCollegeIdIgnoreCase(request.collegeId().trim())) {
            throw ApiException.conflict("An account with this college ID already exists.");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCollegeId(request.collegeId().trim());
        user.setPhone(request.phone().trim());
        user.setBranch(request.branch().trim());
        user.setYear(request.year());

        User saved = userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(saved.getEmail()), UserResponse.from(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // One generic message for both "no such user" and "wrong password" so accounts can't be probed.
        User user = userRepository.findByEmailIgnoreCase(normalize(request.email()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid email or password.");
        }

        return new AuthResponse(jwtService.generateToken(user.getEmail()), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        return UserResponse.from(requireUser(email));
    }

    /** Used by the other services to turn the authenticated email into a User. */
    public User requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ApiException.unauthorized("Your session is no longer valid. Please log in again."));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
