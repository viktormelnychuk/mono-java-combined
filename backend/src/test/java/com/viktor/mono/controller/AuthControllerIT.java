package com.viktor.mono.controller;

import com.viktor.mono.config.AbstractITConfig;
import com.viktor.mono.entity.User;
import com.viktor.mono.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AuthControllerIT extends AbstractITConfig {

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldSuccessfullyRegisterAndObtainJwtTokenAfter() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"test\", \"password\": \"pwd\", \"publicName\": \"pub name\"}")
        ).andExpect(MockMvcResultMatchers.status().is(200));

        Optional<User> test = userRepository.findByUsername("test");
        assertThat(test.isPresent(), equalTo(true));
        User user = test.get();

        assertThat(user.getPublicName(), equalTo("pub name"));
        assertThat(user.getPassword(), not(equalTo("other password")));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"test\", \"password\": \"pwd\"}")
        ).andExpectAll(
                MockMvcResultMatchers.status().is(200),
                MockMvcResultMatchers.jsonPath("$.token", notNullValue()),
                MockMvcResultMatchers.jsonPath("$.type", equalTo("Bearer")),
                MockMvcResultMatchers.jsonPath("$.username", equalTo("test")),
                MockMvcResultMatchers.jsonPath("$.roles", containsInAnyOrder("user"))
        );
    }
}