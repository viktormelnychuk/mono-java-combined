package com.viktor.mono.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {

    @Bean
    public MockRestServiceServer mockRestServiceServer(@Autowired RestTemplate restTemplate) {
        return MockRestServiceServer.bindTo(restTemplate).build();
    }
}
