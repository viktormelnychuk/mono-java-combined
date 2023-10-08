package com.viktor.mono.controller;

import com.viktor.mono.dto.AddMonoTokenRequest;
import com.viktor.mono.dto.UserDto;
import com.viktor.mono.exceptions.MonoInteractionException;
import com.viktor.mono.security.services.UserDetailsImpl;
import com.viktor.mono.service.MonoService;
import com.viktor.mono.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final MonoService monoService;

    public UserController(UserService userService, MonoService monoService) {
        this.userService = userService;
        this.monoService = monoService;
    }


    @GetMapping("/my")
    public ResponseEntity<UserDto> getOne(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getById(userDetails.getId()));
    }

    @PostMapping("/mono-token")
    public ResponseEntity<?> updateMonoToken(@RequestBody AddMonoTokenRequest body, Authentication authentication) throws MonoInteractionException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userService.addMonoToken(userDetails.getId(), body.getMonoToken());
        monoService.updateClientInformation(userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
