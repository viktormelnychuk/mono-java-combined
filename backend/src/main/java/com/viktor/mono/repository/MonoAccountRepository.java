package com.viktor.mono.repository;

import com.viktor.mono.entity.MonoAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonoAccountRepository extends JpaRepository<MonoAccount, Long> {
    Optional<MonoAccount> findByMonoAccountId(String accountId);
    List<MonoAccount> findAllByUserId(Long userId);
}
