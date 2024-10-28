package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;

import java.util.List;

public interface TransactionService {

    List<TransactionDto> parseJson();

    void addTransaction(List<Transaction> accounts);

    void cancelTransaction(List<Long> transactionIds);

    Transaction getTransaction(Long id);
}
