package com.viktor.mono.security.jwt;

import com.viktor.mono.entity.User;
import com.viktor.mono.security.services.UserDetailsImpl;
import com.viktor.mono.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    JwtUtils jwtUtils;
    @Mock
    UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    AuthTokenFilter authTokenFilter;

    @Test
    void shouldNotSetAuthenticationIfAuthHeaderIsInvalid() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("unparsable");

        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(context);
            authTokenFilter.doFilterInternal(request, null, mock(FilterChain.class));

            verify(context, times(0)).setAuthentication(any(Authentication.class));
        }
    }

    @Test
    void shouldNotSetAuthenticationIfJwtIsInvalid() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer sometoken");
        when(jwtUtils.validateJwtToken("sometoken")).thenReturn(false);
        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(context);
            authTokenFilter.doFilterInternal(request, mock(HttpServletResponse.class), mock(FilterChain.class));

            verify(context, times(0)).setAuthentication(any(Authentication.class));
        }
    }

    @Test
    void shouldSetCorrectAuthenticationIfJwtIsValid() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer sometoken");
        when(jwtUtils.validateJwtToken("sometoken")).thenReturn(true);
        when(jwtUtils.getUsernameFromJwtToken("sometoken")).thenReturn("admin");

        User user = new User("admin", "password");
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(context);
            authTokenFilter.doFilterInternal(request, mock(HttpServletResponse.class), mock(FilterChain.class));

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(context).setAuthentication(captor.capture());

            UsernamePasswordAuthenticationToken authenticationToken = captor.getValue();
            assertEquals(userDetails, authenticationToken.getPrincipal());
            assertTrue(authenticationToken.isAuthenticated());
            assertEquals(1, authenticationToken.getAuthorities().size());
            assertEquals("user", new ArrayList<>(authenticationToken.getAuthorities()).get(0).getAuthority());
        }
    }

}