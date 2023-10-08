package com.viktor.mono.repository;

import com.viktor.mono.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.LinkedList;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    LinkedList<Task> findAllByUserIdAndStatusOrderByLastRunAsc(Long userId, String status);
}
