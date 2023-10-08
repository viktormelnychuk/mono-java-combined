package com.viktor.mono.service;

import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.dto.monobank.ClientInfoDto;
import com.viktor.mono.dto.monobank.WebhookDto;
import com.viktor.mono.entity.MonoAccount;
import com.viktor.mono.entity.Transaction;
import com.viktor.mono.entity.User;
import com.viktor.mono.exceptions.MonoInteractionException;
import com.viktor.mono.exceptions.UnprocessableEntityException;
import com.viktor.mono.mapper.MonoAccountMapper;
import com.viktor.mono.mapper.TransactionMapper;
import com.viktor.mono.repository.MonoAccountRepository;
import com.viktor.mono.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonoService {

    @Value("${monobank.api.base-url}")
    private String monoApiBaseUrl;

    @Value("${server.public.url}")
    private String serverUrl;
    private final RestTemplate restTemplate;
    private final MonoAccountRepository monoAccountRepository;
    private final MonoAccountMapper monoAccountMapper;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionService transactionService;
    private final MccService mccService;

    public MonoService(RestTemplate restTemplate,
                       MonoAccountRepository monoAccountRepository,
                       MonoAccountMapper monoAccountMapper,
                       UserRepository userRepository,
                       TransactionMapper transactionMapper,
                       TransactionService transactionService, MccService mccService) {
        this.restTemplate = restTemplate;
        this.monoAccountRepository = monoAccountRepository;
        this.monoAccountMapper = monoAccountMapper;
        this.userRepository = userRepository;
        this.transactionMapper = transactionMapper;
        this.transactionService = transactionService;
        this.mccService = mccService;
    }

    public void updateClientInformation(Long userId) throws MonoInteractionException, UnprocessableEntityException {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getMonoToken() == null) {
            throw new UnprocessableEntityException("user", "monoToken", "mono token is not set");
        }
        List<MonoAccount> existingAccounts = monoAccountRepository.findAllByUserId(user.getId());

        ClientInfoDto clientInfo = queryClientInfo(user.getMonoToken());
        user.setMonoClientId(clientInfo.getClientId());
        user.setMonoName(clientInfo.getName());
        List<MonoAccount> monoAccounts = clientInfo.getAccounts()
                .stream()
                .map((dto) -> {
                    MonoAccount entity = monoAccountMapper.fromDto(dto);
                    entity.setUser(user);
                    return entity;
                })
                .collect(Collectors.toList());
        if (existingAccounts.isEmpty() || existingAccounts.size() != monoAccounts.size()) {
            monoAccountRepository.deleteAll(existingAccounts);
            monoAccountRepository.saveAll(monoAccounts);
        }
        userRepository.save(user);
    }


    public void createWebhook(Long userId) throws MonoInteractionException, UnprocessableEntityException {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getMonoToken() == null) {
            throw new UnprocessableEntityException("user", "monoToken", "mono token is not set");
        }

        addWebhook(user.getMonoToken(), userId);
    }

    public void handleWebhook(WebhookDto dto) {
        MonoAccount monoAccount = monoAccountRepository.findByMonoAccountId(dto.getData().getAccount())
                .orElseThrow(() -> {
                    return new RuntimeException(String.format("Mono account with id %s does not exist", dto.getData().getAccount()));
                });
        TransactionDto transactionDto = transactionMapper.fromStatementItem(dto.getData().getStatementItem(), mccService);
        transactionService.create(transactionDto, monoAccount);
    }

    private ClientInfoDto queryClientInfo(String monoToken) throws MonoInteractionException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Token", monoToken);
        HttpEntity<ClientInfoDto> entity = new HttpEntity<>(headers);
        String url = monoApiBaseUrl + "/personal/client-info";
        try {
            ResponseEntity<ClientInfoDto> resp = restTemplate.exchange(url, HttpMethod.GET, entity, ClientInfoDto.class);
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new MonoInteractionException(resp.toString());
            }
            return resp.getBody();
        } catch (RestClientException e) {
            throw new MonoInteractionException(e.getMessage());
        }
    }

    private void addWebhook(String monoToken, Long userId) throws MonoInteractionException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Token", monoToken);
        Map<String, String> body = new HashMap<>();
        body.put("webHookUrl", String.format("%s/api/v1/webhook/%d", serverUrl, userId));
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        String url = monoApiBaseUrl + "/personal/webhook";
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new MonoInteractionException(resp.getBody());
        }
    }
}
