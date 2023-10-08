package com.viktor.mono.security.services;

import com.viktor.mono.entity.User;
import com.viktor.mono.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldThrowExceptionIfUserNotFound() {
        when(userRepository.findByUsername("test")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("test");
        });
        assertEquals("User with username test not found", exception.getMessage());
    }

    @Test
    void shouldReturnCorrectUserDetailsWhenUserFound() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("password");
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("test");

        assertEquals("test", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("user", new ArrayList<>(userDetails.getAuthorities()).get(0).getAuthority());
    }

}