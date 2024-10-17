package ru.t1.java.demo.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.t1.java.demo.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Transaction> findByIsNeedRetry(Boolean isNeedRetry);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Transaction> findById(Long Long);
}

