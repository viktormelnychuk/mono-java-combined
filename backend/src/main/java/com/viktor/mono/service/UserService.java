package com.viktor.mono.service;

import com.viktor.mono.dto.UserDto;
import com.viktor.mono.entity.Task;
import com.viktor.mono.entity.User;
import com.viktor.mono.exceptions.EntityNotFoundException;
import com.viktor.mono.mapper.UserMapper;
import com.viktor.mono.repository.TaskRepository;
import com.viktor.mono.repository.UserRepository;
import com.viktor.mono.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public UserDto getById(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("user"));
        return mapper.fromEntity(user);
    }

    public void addMonoToken(Long userId, String monoToken) {
        User user = repository.findById(userId).orElseThrow(() -> new EntityNotFoundException("user"));
        user.setMonoToken(monoToken);
        repository.save(user);
    }

    public UserDto getCurrent() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User entity = repository.findById(userDetails.getId()).orElseThrow();
        return mapper.fromEntity(entity);
    }

}
