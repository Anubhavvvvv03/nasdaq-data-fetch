package com.hdfc.nasdaq_assignment.service;

import com.hdfc.nasdaq_assignment.config.JwtUtils;
import com.hdfc.nasdaq_assignment.dto.AuthRequest;
import com.hdfc.nasdaq_assignment.dto.AuthResponse;
import com.hdfc.nasdaq_assignment.exception.AuthException;
import com.hdfc.nasdaq_assignment.exception.ErrorCode;
import com.hdfc.nasdaq_assignment.model.User;
import com.hdfc.nasdaq_assignment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthResponse signup(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException(ErrorCode.USER_ALREADY_EXISTS, "Username is already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        String token = jwtUtils.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername());
    }

    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AuthException(ErrorCode.UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtUtils.generateToken(request.getUsername());
        return new AuthResponse(token, request.getUsername());
    }
}
