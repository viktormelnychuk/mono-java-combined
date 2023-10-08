package com.viktor.mono.controller;

import com.viktor.mono.dto.UserDto;
import com.viktor.mono.dto.monobank.WebhookDto;
import com.viktor.mono.exceptions.MonoInteractionException;
import com.viktor.mono.service.MonoService;
import com.viktor.mono.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook")
public class WebhookController {

    private final MonoService monoService;
    private final UserService userService;

    public WebhookController(UserService userService, MonoService monoService) {
        this.monoService = monoService;
        this.userService = userService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> receiveWebhook(@PathVariable Long id, @RequestBody WebhookDto dto) {
        monoService.handleWebhook(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> confirmWebhook(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createWebhook() throws MonoInteractionException {
        UserDto current = userService.getCurrent();
        monoService.createWebhook(current.getId());
        return ResponseEntity.ok().build();
    }
}
