package ru.t1.java.demo.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.t1.java.demo.model.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Client> findById(Long aLong);
}