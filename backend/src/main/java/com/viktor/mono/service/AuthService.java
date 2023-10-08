package com.viktor.mono.service;

import com.viktor.mono.dto.JwtResponse;
import com.viktor.mono.dto.LoginRequest;
import com.viktor.mono.dto.SignupRequest;
import com.viktor.mono.entity.User;
import com.viktor.mono.exceptions.UnprocessableEntityException;
import com.viktor.mono.repository.UserRepository;
import com.viktor.mono.security.jwt.JwtUtils;
import com.viktor.mono.security.services.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       AuthenticationManager authManager
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authManager;
    }
    public JwtResponse signin(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles);
    }

    public void signup(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new UnprocessableEntityException("user", "username", "already taken");
        }

        // Create new user's account
        User user = new User(signupRequest.getUsername(), passwordEncoder.encode(signupRequest.getPassword()));
        user.setPublicName(signupRequest.getPublicName());

        userRepository.save(user);

    }
}
