package com.viktor.mono.controller;

import com.viktor.mono.config.AbstractITConfig;
import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.entity.Transaction;
import com.viktor.mono.repository.TransactionRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


class TransactionControllerIT extends AbstractITConfig {

    @Autowired
    TransactionRepository repository;

    @Test
    @Sql({
            "/data/common/createUsers.sql",
            "/data/common/addMonoAccounts.sql",
            "/data/TransactionControllerIT/createTransactions.sql"
    })
    @WithUserDetails("admin")
    void shouldGetSingleTransactionById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transactions/1"))
                .andExpectAll(
                        MockMvcResultMatchers.status().is(200),
                        MockMvcResultMatchers.jsonPath("$.id", equalTo(1)),
                        MockMvcResultMatchers.jsonPath("$.description", equalTo("description")),
                        MockMvcResultMatchers.jsonPath("$.monoAccount", notNullValue())
                );
    }

    @Test
    @Sql({
            "/data/common/createUsers.sql",
            "/data/common/addMonoAccounts.sql",
            "/data/TransactionControllerIT/createTransactions.sql"
    })
    @WithUserDetails("admin")
    void shouldGetAllTransactions() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transactions"))
                .andExpectAll(
                        MockMvcResultMatchers.status().is(200),
                        MockMvcResultMatchers.jsonPath("$.size()", equalTo(2))
                );
    }

    @Test
    @Sql({
            "/data/common/createUsers.sql",
    })
    @WithUserDetails("admin")
    void canCreateNewTransaction() throws Exception {
        BlockingQueue<List<TransactionDto>> socketMessages = new ArrayBlockingQueue<>(1);
        subscribeToTransactionsMessages(socketMessages);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monoId\":\"first one\",\"amount\":11111,\"comment\":\"transaction comment\"}")
                )
                .andExpectAll(
                        MockMvcResultMatchers.status().is(201)
                );

        List<Transaction> all = repository.findAll();
        assertThat(all.size(), equalTo(1));
        assertThat(all.get(0).getMonoId(), equalTo("first one"));
        assertThat(all.get(0).getAmount(), equalTo(11111));
        assertThat(all.get(0).getComment(), equalTo("transaction comment"));

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
        assertThat(transactionsFromSocket.get(0).getMonoId(), equalTo("first one"));
        assertThat(transactionsFromSocket.get(0).getAmount(), equalTo(11111));
        assertThat(transactionsFromSocket.get(0).getComment(), equalTo("transaction comment"));
    }
}