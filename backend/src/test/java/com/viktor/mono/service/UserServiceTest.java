package com.viktor.mono.service;

import com.viktor.mono.dto.UserDto;
import com.viktor.mono.entity.User;
import com.viktor.mono.exceptions.EntityNotFoundException;
import com.viktor.mono.mapper.UserMapper;
import com.viktor.mono.mapper.UserMapperImpl;
import com.viktor.mono.repository.UserRepository;
import com.viktor.mono.security.services.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Spy
    UserMapperImpl userMapper;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void shouldReturnOneById() {
        User user = new User("admin", "password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto dto = userService.getById(1L);
        assertEquals(user.getUsername(), dto.getUsername());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.getById(1L);
        });

        assertEquals("user not found", exception.getMessage());
    }

    @Test
    void shouldSaveMonoToken() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.addMonoToken(1L, "token");

        verify(user).setMonoToken("token");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundTryingToAddToken() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.addMonoToken(1L, "");
        });

        assertEquals("user not found", exception.getMessage());
    }

    @Test
    void shouldReturnCurrentAuthenticatedUser() {
        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
            when(userDetails.getId()).thenReturn(1L);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(context.getAuthentication()).thenReturn(authentication);
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(context);
            User user = new User("admin", "test");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserDto current = userService.getCurrent();

            assertEquals(user.getUsername(), current.getUsername());
        }
    }
}