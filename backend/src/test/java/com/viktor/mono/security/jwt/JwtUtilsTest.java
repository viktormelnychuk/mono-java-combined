package com.viktor.mono.security.jwt;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viktor.mono.entity.User;
import com.viktor.mono.security.services.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.event.LoggingEvent;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    JwtUtils jwtUtils;

    @Test
    void shouldGenerateCorrectJwtWithPayload() throws JsonProcessingException {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "aaaaaaaaaabbbbbbbbbccccccccceeeeeee===========");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 10000);
        User user = new User("admin", "password");
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtUtils.generateJwtToken(authentication);
        String[] tokenParts = token.split("\\.");
        assertTrue(StringUtils.isNotBlank(token));
        assertEquals(3, tokenParts.length);

        String stringPayload = new String(Base64.getDecoder().decode(tokenParts[1]));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> payload = objectMapper.readValue(stringPayload, new TypeReference<>() {
        });
        assertEquals("admin", payload.get("sub"));
        assertEquals((Integer) payload.get("iat") + 10, payload.get("exp"));
        long epochSecondsNow = Instant.now().toEpochMilli() / 1000;
        Long issuedAt = Long.valueOf((Integer) payload.get("iat"));
        assertTrue(epochSecondsNow - 1 <= issuedAt && issuedAt <= epochSecondsNow + 1);
    }


    @Test
    void shouldReturnCorrectUsernameFromToken() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "aaaaaaaaaabbbbbbbbbccccccccceeeeeee===========");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 10000);
        User user = new User("admin", "password");
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtUtils.generateJwtToken(authentication);

        String usernameFromJwtToken = jwtUtils.getUsernameFromJwtToken(token);
        assertEquals("admin", usernameFromJwtToken);
    }

    @Test
    void shouldReturnFalseWhenValidatingTokenIfTokenInvalid() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "aaaaaaaaaabbbbbbbbbccccccccceeeeeee===========");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -10000);
        Logger logger = (Logger) LoggerFactory.getLogger(JwtUtils.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        User user = new User("admin", "password");
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtUtils.generateJwtToken(authentication);
        boolean result = jwtUtils.validateJwtToken(token);
        assertTrue(listAppender.list.get(0).getMessage().startsWith("JWT token is expired:"));
        assertEquals(Level.ERROR, listAppender.list.get(0).getLevel());
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenValidatingTokenIfTokenExpired() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "aaaaaaaaaabbbbbbbbbccccccccceeeeeee===========");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -10000);
        Logger logger = (Logger) LoggerFactory.getLogger(JwtUtils.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        boolean result = jwtUtils.validateJwtToken("some");
        assertTrue(listAppender.list.get(0).getMessage().startsWith("Invalid JWT token:"));
        assertEquals(Level.ERROR, listAppender.list.get(0).getLevel());
        assertFalse(result);
    }
}