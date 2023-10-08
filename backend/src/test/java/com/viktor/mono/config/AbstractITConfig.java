package com.viktor.mono.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.viktor.mono.dto.TransactionDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.lang.Nullable;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

@ActiveProfiles("test")
@ResetDB
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration
public class AbstractITConfig {

    @Autowired
    protected MockMvc mockMvc;
    @LocalServerPort
    private Integer port;

    @Autowired
    protected MockRestServiceServer mockServer;

    @AfterEach
    void after() {
        mockServer.verify();
    }

    protected String readJsonAsString(String path) throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream(path))) {
            return new String(is.readAllBytes());
        }
    }

    protected void subscribeToTransactionsMessages(BlockingQueue<List<TransactionDto>> socketMessages) throws ExecutionException, InterruptedException, TimeoutException {
        String url = String.format("ws://localhost:%d/socket", port);

        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new StringMessageConverter());
        client.setMessageConverter(new MappingJackson2MessageConverter());



        client.connectAsync(url, new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        session.subscribe("/user/admin/transactions", new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return new TypeReference<List<TransactionDto>>() {
                                }.getType();
                            }

                            @SneakyThrows
                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                ObjectMapper objectMapper = new ObjectMapper();
                                objectMapper.registerModule(new JavaTimeModule());
                                String jsonString = objectMapper.writeValueAsString(payload);
                                List<TransactionDto> transactions = objectMapper.readValue(jsonString, new TypeReference<List<TransactionDto>>() {
                                });
                                socketMessages.add(transactions);
                            }
                        });
                    }
                })
                .get(1, SECONDS);
    }
}
