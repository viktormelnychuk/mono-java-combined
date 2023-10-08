package com.viktor.mono.task;

import com.viktor.mono.entity.Task;
import com.viktor.mono.entity.Transaction;
import com.viktor.mono.entity.User;
import com.viktor.mono.repository.TaskRepository;
import com.viktor.mono.repository.UserRepository;
import com.viktor.mono.service.UserService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@Transactional
public class ReadOldTransactionsTask {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ReadOldTransactionsTask(UserService userService, UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Scheduled(fixedRateString = "${scheduled.read_old_task.interval}")
    public void getUserTransactions() {
        List<User> usersAllowedToRun = getUsersWithEnabledTransactionsFetching();
        usersAllowedToRun.forEach((u) -> {
            LocalDateTime lastRecorderTransaction = getLastRecordedTransactionTime(u.getTransactions());
            LocalDateTime targetDate = lastRecorderTransaction.plusDays(29);
            Task task = new Task();
            task.setStatus("created");
            taskRepository.save(task);
            log.info("Sending request to mono for user {} with from = [{}] and to = [{}]", u.getUsername(), lastRecorderTransaction, targetDate);
            // assuming task run successfully
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            task.setLastRun(LocalDateTime.now());
            task.setStatus("completed");
            taskRepository.save(task);
        });
    }

    public List<User> getUsersWithEnabledTransactionsFetching() {
        List<User> allUsers = userRepository.findAllByEnabledOldTransactionsFetching(true);
        List<User> completed = allUsers.stream().filter((u) -> {
            LinkedList<Task> allTasks = taskRepository.findAllByUserIdAndStatusOrderByLastRunAsc(u.getId(), "completed");
            if (allTasks.isEmpty()) {
                // user does not have tasks but allows to run
                return true;
            }
            Task last = allTasks.getLast();
            LocalDateTime now = LocalDateTime.now();
            return ChronoUnit.SECONDS.between(last.getLastRun(), now) > 1000;

        }).collect(Collectors.toList());
        return completed;
    }

    private LocalDateTime getLastRecordedTransactionTime(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return LocalDateTime.now();
        }

        Transaction transaction = transactions.stream().min(Comparator.comparing(Transaction::getTime)).orElse(null);
        return transaction.getTime();
    }
}
