package com.viktor.mono.service;

import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.dto.monobank.*;
import com.viktor.mono.entity.MonoAccount;
import com.viktor.mono.entity.Transaction;
import com.viktor.mono.entity.User;
import com.viktor.mono.exceptions.MonoInteractionException;
import com.viktor.mono.exceptions.UnprocessableEntityException;
import com.viktor.mono.mapper.MonoAccountMapper;
import com.viktor.mono.mapper.MonoAccountMapperImpl;
import com.viktor.mono.mapper.TransactionMapper;
import com.viktor.mono.mapper.TransactionMapperImpl;
import com.viktor.mono.repository.MonoAccountRepository;
import com.viktor.mono.repository.TransactionRepository;
import com.viktor.mono.repository.UserRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonoServiceTest {
    @Mock
    RestTemplate restTemplate;
    @Mock
    MonoAccountRepository monoAccountRepository;
    @Spy
    MonoAccountMapperImpl monoAccountMapper;
    @Mock
    UserRepository userRepository;
    @Spy
    TransactionMapperImpl transactionMapper;
    @Mock
    TransactionService transactionService;
    @Mock
    MccService mccService;

    @InjectMocks
    MonoService monoService;

    @Test
    void sendsCorrectRequestWhenCreatingWebhook() throws MonoInteractionException {
        User user = new User();
        user.setMonoToken("some token");
        ReflectionTestUtils.setField(monoService, "serverUrl", "http://localhost");
        ReflectionTestUtils.setField(monoService, "monoApiBaseUrl", "http://monobank");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<String> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity<Map<String, String>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(urlCaptor.capture(), eq(HttpMethod.POST), entityCaptor.capture(), eq(String.class))).thenReturn(response);
        monoService.createWebhook(1L);
        String url = urlCaptor.getValue();
        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        Map<String, String> body = entityCaptor.getValue().getBody();
        assertEquals("http://monobank/personal/webhook", url);
        assertEquals("some token", headers.get("X-Token").get(0));
        assertEquals(1, body.size());
        assertEquals("http://localhost/api/v1/webhook/1", body.get("webHookUrl"));
    }

    @Test
    void throwsExceptionIfExchangeWithMonobankApiFailed() {
        User user = new User();
        user.setMonoToken("some token");
        ReflectionTestUtils.setField(monoService, "serverUrl", "http://localhost");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<String> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(response.getBody()).thenReturn("something bad happened");

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(response);
        MonoInteractionException exception = assertThrows(MonoInteractionException.class, () -> {
            monoService.createWebhook(1L);
        });
        assertEquals("Mono interaction failed with error something bad happened", exception.getMessage());
    }

    @Test
    void shouldThrowErrorWhenCreatingWebhookIfMonoTokenIsNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () -> {
            monoService.createWebhook(1L);
        });
        assertEquals("can't process user because [monoToken] mono token is not set", exception.getMessage());
    }

    @Test
    void shouldThrowErrorWhenRequestingInfoIfMonoTokenIsNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () -> {
            monoService.updateClientInformation(1L);
        });
        assertEquals("can't process user because [monoToken] mono token is not set", exception.getMessage());
    }

    @Test
    void shouldCallCorrectAPIWhenRequestingClientInfo() throws MonoInteractionException {
        User user = new User();
        user.setMonoToken("some token");
        ReflectionTestUtils.setField(monoService, "serverUrl", "http://localhost");
        ReflectionTestUtils.setField(monoService, "monoApiBaseUrl", "http://monobank");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<ClientInfoDto> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getBody()).thenReturn(mock(ClientInfoDto.class));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity<Map<String, String>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(urlCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(), eq(ClientInfoDto.class))).thenReturn(response);
        monoService.updateClientInformation(1L);
        String url = urlCaptor.getValue();
        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        Map<String, String> body = entityCaptor.getValue().getBody();
        assertEquals("http://monobank/personal/client-info", url);
        assertEquals("some token", headers.get("X-Token").get(0));
        assertFalse(entityCaptor.getValue().hasBody());
    }

    @Test
    void throwsExceptionIfClientInfoCallFailed() throws MonoInteractionException {
        User user = new User();
        user.setMonoToken("some token");
        ReflectionTestUtils.setField(monoService, "serverUrl", "http://localhost");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<ClientInfoDto> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(response.toString()).thenReturn("string response");

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(), eq(ClientInfoDto.class))).thenReturn(response);
        MonoInteractionException exception = assertThrows(MonoInteractionException.class, () -> {
            monoService.updateClientInformation(1L);
        });
        assertEquals("Mono interaction failed with error string response", exception.getMessage());
    }

    @Test
    void shouldUpdateUserAfterInformationWasRequested() throws MonoInteractionException {
        User user = new User();
        user.setMonoToken("some token");
        ReflectionTestUtils.setField(monoService, "serverUrl", "http://localhost");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<ClientInfoDto> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        ClientInfoDto dto = buildClientInfoDto();
        when(response.getBody()).thenReturn(dto);

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(), eq(ClientInfoDto.class))).thenReturn(response);
        monoService.updateClientInformation(1L);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(dto.getName(), user.getMonoName());
        assertEquals(dto.getClientId(), user.getMonoClientId());
    }

    @Test
    void shouldNotUpdateUserAccountsIfSizeOfExistingMatchesQueries() throws MonoInteractionException {
        User user = new User();
        user.setMonoToken("some token");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(monoAccountRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(mock(MonoAccount.class)));

        ResponseEntity<ClientInfoDto> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        ClientInfoDto dto = buildClientInfoDto();
        when(response.getBody()).thenReturn(dto);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(), eq(ClientInfoDto.class))).thenReturn(response);

        monoService.updateClientInformation(1L);

        verify(monoAccountRepository, times(0)).deleteAll(anyCollection());
        verify(monoAccountRepository, times(0)).saveAll(anyCollection());
    }

    @Test
    void shouldUpdateUserAccountsIfNotExistingAccounts() throws MonoInteractionException {
        User user = new User();
        user.setMonoToken("some token");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        List<MonoAccount> existingAccounts = Collections.emptyList();
        when(monoAccountRepository.findAllByUserId(1L)).thenReturn(existingAccounts);

        ResponseEntity<ClientInfoDto> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        ClientInfoDto dto = buildClientInfoDto();
        when(response.getBody()).thenReturn(dto);
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(), eq(ClientInfoDto.class))).thenReturn(response);

        monoService.updateClientInformation(1L);

        verify(monoAccountRepository).deleteAll(existingAccounts);
        ArgumentCaptor<List<MonoAccount>> accountsCaptor = ArgumentCaptor.forClass(List.class);
        verify(monoAccountRepository).saveAll(accountsCaptor.capture());
        List<MonoAccount> savedAccounts = accountsCaptor.getValue();
        assertEquals(dto.getAccounts().size(), savedAccounts.size());
        assertEquals(dto.getAccounts().get(0).getSendId(), savedAccounts.get(0).getSendId());
        assertEquals(dto.getAccounts().get(0).getType(), savedAccounts.get(0).getType());
        assertEquals(dto.getAccounts().get(0).getId(), savedAccounts.get(0).getMonoAccountId());
        assertEquals(user, savedAccounts.get(0).getUser());
    }

    @Test
    void shouldNotSaveTransactionIfMonoAccountDoesntExistWhenHandlingWebhook() {
        when(monoAccountRepository.findByMonoAccountId("account id")).thenReturn(Optional.empty());
        WebhookDto dto = new WebhookDto();
        WebhookDataDto dataDto = new WebhookDataDto();
        dataDto.setAccount("account id");
        dto.setData(dataDto);
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            monoService.handleWebhook(dto);
        });
        assertEquals("Mono account with id account id does not exist", runtimeException.getMessage());
    }

    @Test
    void shouldCorrectlySaveTransactionWhenHandlingWebhook() {
        MonoAccount monoAccount = mock(MonoAccount.class);
        when(monoAccountRepository.findByMonoAccountId("acc_id")).thenReturn(Optional.of(monoAccount));

        WebhookDto webhookDto = new WebhookDto();
        WebhookDataDto webhookDataDto = new WebhookDataDto();
        webhookDataDto.setAccount("acc_id");
        StatementItemDto statementItemDto = getStatementItemDto();
        webhookDataDto.setStatementItem(statementItemDto);
        webhookDto.setData(webhookDataDto);
        ArgumentCaptor<TransactionDto> transactionCaptor = ArgumentCaptor.forClass(TransactionDto.class);
        ArgumentCaptor<MonoAccount> monoAccountCaptor = ArgumentCaptor.forClass(MonoAccount.class);

        monoService.handleWebhook(webhookDto);

        verify(transactionService).create(transactionCaptor.capture(), monoAccountCaptor.capture());

        TransactionDto transaction = transactionCaptor.getValue();
        assertEquals(monoAccount, monoAccountCaptor.getValue());
        assertEquals(100L, transaction.getAmount());
        assertEquals("description", transaction.getDescription());
        assertEquals("stmnt_id", transaction.getMonoId());
    }

    private static StatementItemDto getStatementItemDto() {
        StatementItemDto statementItemDto = new StatementItemDto();
        statementItemDto.setId("stmnt_id");
        statementItemDto.setTime(1696373171568L);
        statementItemDto.setDescription("description");
        statementItemDto.setMcc(1L);
        statementItemDto.setOriginalMcc(1L);
        statementItemDto.setAmount(100L);
        statementItemDto.setOperationAmount(100L);
        statementItemDto.setCurrencyCode(10);
        statementItemDto.setCommissionRate(4);
        statementItemDto.setCashbackAmount(15L);
        statementItemDto.setBalance(10003L);
        statementItemDto.setHold(false);
        return statementItemDto;
    }

    private ClientInfoDto buildClientInfoDto() {
        ClientInfoDto dto = new ClientInfoDto();
        dto.setName("Firt Last");
        dto.setPermissions("pwd");
        dto.setWebHookUrl("http://localhost");
        MonoAccountDto accountDto = new MonoAccountDto();
        accountDto.setId("1");
        accountDto.setMonoAccountId("some id");
        accountDto.setSendId("sendId");
        accountDto.setType("type");
        dto.setAccounts(Collections.singletonList(accountDto));

        return dto;
    }
}