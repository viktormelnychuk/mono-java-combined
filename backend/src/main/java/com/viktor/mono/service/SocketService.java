package com.viktor.mono.service;

import com.viktor.mono.dto.TransactionDto;
import com.viktor.mono.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocketService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public SocketService(SimpMessagingTemplate template) {
        this.simpMessagingTemplate = template;
    }

    public void notifyTransaction(UserDto userDto, List<TransactionDto> dto) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor
                .create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(userDto.getUsername());
        headerAccessor.setLeaveMutable(true);

        simpMessagingTemplate.convertAndSendToUser(userDto.getUsername(), "/transactions", dto, headerAccessor.getMessageHeaders());
    }

}
