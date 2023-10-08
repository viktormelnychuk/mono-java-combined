package com.viktor.mono.controller;

import com.viktor.mono.config.AbstractITConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.swing.text.DateFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerIT extends AbstractITConfig {

    @Value("${monobank.api.base-url}")
    private String monoApiBaseUrl;

    @Test
    @Sql(
            "/data/common/createUsers.sql"
    )
    void uprocessableEntityException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"admin\", \"password\": \"pwd\", \"publicName\": \"pub name\"}")
        ).andExpectAll(
                MockMvcResultMatchers.status().is(400),
                MockMvcResultMatchers.jsonPath("$.status", equalTo("BAD_REQUEST")),
                MockMvcResultMatchers.jsonPath("$.timestamp", equalTo(timeNow(0))),
                MockMvcResultMatchers.jsonPath("$.message", equalTo("can't process entity")),
                MockMvcResultMatchers.jsonPath("$.debugMessage", equalTo("can't process user because [username] already taken"))
        );
    }

    @Test
    @Sql(
            "/data/common/createUsers.sql"
    )
    @WithUserDetails("admin")
    void entityNotFoundException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transactions/1")
        ).andExpectAll(
                MockMvcResultMatchers.status().is(404),
                MockMvcResultMatchers.jsonPath("$.status", equalTo("NOT_FOUND")),
                MockMvcResultMatchers.jsonPath("$.timestamp", equalTo(timeNow(0))),
                MockMvcResultMatchers.jsonPath("$.message", equalTo("not found")),
                MockMvcResultMatchers.jsonPath("$.debugMessage", equalTo("transaction not found"))
        );
    }

    @Test
    @Sql(
            "/data/common/createUsers.sql"
    )
    @WithUserDetails("admin")
    void monoInteractionException() throws Exception {
        String url = monoApiBaseUrl + "/personal/client-info";
        mockServer.expect(requestTo(url))
                .andExpect(MockRestRequestMatchers.header("X-Token", "new user token"))
                .andRespond(
                        MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("{\"error\":\" find me\"}")
                );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/mono-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monoToken\":\"new user token\"}"))
                .andExpectAll(
                        MockMvcResultMatchers.status().is(500),
                        MockMvcResultMatchers.jsonPath("$.status", equalTo("INTERNAL_SERVER_ERROR")),
                        MockMvcResultMatchers.jsonPath("$.timestamp", equalTo(timeNow(0))),
                        MockMvcResultMatchers.jsonPath("$.message", equalTo("error sending request to monobank api")),
                        MockMvcResultMatchers.jsonPath("$.debugMessage", equalTo("Mono interaction failed with error 400 Bad Request: \"{\"error\":\" find me\"}\""))
                );

    }

    private String timeNow(int seconds) {
        return DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss").format(LocalDateTime.now());
    }
}