package com.viktor.mono.controller;

import com.viktor.mono.config.AbstractITConfig;
import com.viktor.mono.entity.MonoAccount;
import com.viktor.mono.entity.User;
import com.viktor.mono.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;


class UserControllerIT extends AbstractITConfig {

    @Autowired
    UserRepository userRepository;

    @Value("${monobank.api.base-url}")
    private String monoApiBaseUrl;

    @Test
    @WithUserDetails("admin")
    @Sql("/data/common/createUsers.sql")
    void addMonoToken() throws Exception {
        String url = monoApiBaseUrl + "/personal/client-info";
        String clientInfoResponse = readJsonAsString("/data/mono/client_info.json");
        mockServer.expect(requestTo(url))
                .andExpect(MockRestRequestMatchers.header("X-Token", "new user token"))
                .andRespond(
                        MockRestResponseCreators.withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(clientInfoResponse)
                );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/mono-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monoToken\":\"new user token\"}"))
                .andExpect(MockMvcResultMatchers.status().is(200));

        User user = userRepository.findByUsername("admin").orElseThrow();
        assertThat(user.getMonoToken(), equalTo("new user token"));
        assertThat(user.getMonoName(), equalTo("Test User"));
        assertThat(user.getMonoClientId(), equalTo("client_id"));
        assertThat(user.getMonoAccounts().size(), equalTo(2));
        assertThat(user.getMonoAccounts().stream().map(MonoAccount::getMonoAccountId).toList(),
                containsInAnyOrder("account_1_id", "account_2_id"));
    }

    @Test
    @WithUserDetails("admin")
    @Sql({
            "/data/common/createUsers.sql",
            "/data/common/addMonoAccounts.sql"
    })
    void returnsCurrentUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/my"))
                .andExpectAll(
                        MockMvcResultMatchers.status().is(200),
                        MockMvcResultMatchers.jsonPath("$.id", equalTo(1)),
                        MockMvcResultMatchers.jsonPath("$.username", equalTo("admin")),
                        MockMvcResultMatchers.jsonPath("$.monoAccounts.size()", equalTo(1)),
                        MockMvcResultMatchers.jsonPath("$.monoAccounts[0].id", equalTo("1")),
                        MockMvcResultMatchers.jsonPath("$.monoAccounts[0].monoAccountId", equalTo("mono_acc_id")),
                        MockMvcResultMatchers.jsonPath("$.monoAccounts[0].sendId", equalTo("send_id")),
                        MockMvcResultMatchers.jsonPath("$.monoAccounts[0].currencyCode", equalTo(25))
                );
    }
}