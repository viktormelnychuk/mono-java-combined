package com.viktor.mono.service;

import com.viktor.mono.dto.JwtResponse;
import com.viktor.mono.dto.LoginRequest;
import com.viktor.mono.dto.SignupRequest;
import com.viktor.mono.entity.User;
import com.viktor.mono.exceptions.UnprocessableEntityException;
import com.viktor.mono.repository.UserRepository;
import com.viktor.mono.security.jwt.JwtUtils;
import com.viktor.mono.security.services.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtUtils jwtUtils;
    @Mock
    AuthenticationManager authenticationManager;

    @InjectMocks
    AuthService authService;

    @Test
    void shouldThrowErrorIfUserExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("admin");

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () -> {
            authService.signup(signupRequest);
        });
        assertEquals("can't process user because [username] already taken", exception.getMessage());
    }

    @Test
    void shouldSaveNewUserIfUsernameDoesNotExist() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("admin");
        signupRequest.setPassword("test");

        when(passwordEncoder.encode("test")).thenReturn("encoded password");

        authService.signup(signupRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedEntity = captor.getValue();
        assertEquals("admin", savedEntity.getUsername());
        assertEquals("encoded password", savedEntity.getPassword());
    }

    @Test
    void shouldReturnValidJwtResponse() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("test");
        loginRequest.setPassword("test");

        User user = new User("test", "test");
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt token");


        JwtResponse jwt = authService.signin(loginRequest);

        assertEquals("jwt token", jwt.getToken());
        assertEquals("Bearer", jwt.getType());
        assertEquals("test", jwt.getUsername());
        assertEquals(1, jwt.getRoles().size());
        assertEquals("user", jwt.getRoles().get(0));
    }

}