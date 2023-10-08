package com.viktor.mono.controller;

import com.viktor.mono.config.AbstractITConfig;
import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.entity.Transaction;
import com.viktor.mono.repository.TransactionRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;

class WebhookControllerIT extends AbstractITConfig {
    @Value("${monobank.api.base-url}")
    private String monoApiBaseUrl;

    @Value("${server.public.url}")
    private String serverUrl;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldRespondSuccessWhenMonobankConfirmsWebhook() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/webhook/1")).andExpect(
                MockMvcResultMatchers.status().is(200)
        );
    }

    @Test
    @Sql("/data/common/createUsers.sql")
    @WithUserDetails("admin")
    void shouldSendRequestToCreateWebhookWhenUserCreatesWebhook() throws Exception {
        String url = monoApiBaseUrl + "/personal/webhook";
        mockServer.expect(requestTo(url))
                .andExpect(jsonPath("$.webHookUrl", equalTo(serverUrl + "/api/v1/webhook/1")))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/webhook/create")
        ).andExpect(MockMvcResultMatchers.status().is(200));
    }

    @Test
    @Sql({
            "/data/common/createUsers.sql",
            "/data/common/addMonoAccounts.sql"
    })
    void shouldHandleWebhookCorrectly() throws Exception {
        String json = readJsonAsString("/data/WebhookControllerIT/webhook_request.json");
        BlockingQueue<List<TransactionDto>> socketMessages = new ArrayBlockingQueue<>(1);
        subscribeToTransactionsMessages(socketMessages);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/webhook/1")
                        .contentType(MediaType.APPLICATION_JSON).content(json)
        ).andExpect(MockMvcResultMatchers.status().is(200));


        List<Transaction> all = transactionRepository.findAll();
        assertThat(all.size(), equalTo(1));
        Transaction transaction = all.get(0);
        assertThat(transaction.getMonoId(), equalTo("trans_id"));
        assertThat(transaction.getTime(), equalTo(LocalDateTime.of(2023, 10, 2, 18, 3, 40)));

        AtomicReference<List<TransactionDto>> transactionsFromSocketAtomic = new AtomicReference<>();
        Awaitility.await()
                .atMost(10, SECONDS)
                .pollInterval(1, SECONDS)
                .until(() -> {
                    List<TransactionDto> val = socketMessages.poll();
                    if (Objects.nonNull(val)) {
                        transactionsFromSocketAtomic.set(val);
                        return true;
                    }
                    return false;
                });
        List<TransactionDto> transactionsFromSocket = transactionsFromSocketAtomic.get();
        assertThat(transactionsFromSocket.size(), equalTo(1));
        assertThat(transactionsFromSocket.get(0).getMonoId(), equalTo("trans_id"));
        assertThat(transactionsFromSocket.get(0).getId(), equalTo(transaction.getId()));
    }

}