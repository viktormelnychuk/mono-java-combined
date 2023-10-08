package com.viktor.mono.repository;

import com.viktor.mono.entity.Mcc;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MccRepository extends JpaRepository<Mcc, Long> {

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE mcc", nativeQuery = true)
    void truncate();

    Mcc findByCode(Long code);
}
