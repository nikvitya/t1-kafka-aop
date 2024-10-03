package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.aop.Metric;
import ru.t1.java.demo.exception.ConflictException;
import ru.t1.java.demo.exception.NotFoundException;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.mapper.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final KafkaTransactionProducer kafkaTransactionProducer;

    @Metric
    @Override
    public List<TransactionDto> parseJson() {
        ObjectMapper mapper = new ObjectMapper();

        TransactionDto[] transactions = new TransactionDto[0];
        try {
            transactions = mapper.readValue(new File("src/main/resources/MOCK_TRANSACTION_DATA.json"), TransactionDto[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Arrays.asList(transactions);
    }

    @Metric
    @Override
    public void addTransaction(List<Transaction> transactions) {
        List<Transaction> validatedTransactions = new ArrayList<>();

        for (Transaction transaction : transactions) {
            Long clientId = transaction.getClientId();
            Long accountId = transaction.getAccountId();

            Client client = clientRepository.findById(clientId)
                    .orElseThrow(()->new NotFoundException(String.format("Пользователь с client_id = %, не найден", clientId)));

            Account account = accountRepository.findById(accountId)
                    .orElseThrow(()->new NotFoundException(String.format("Пользователь с account_id = %, не найден", accountId)));

            if (account.getClientId() != client.getId()) {
                throw new ConflictException(String.format("Клиент с id = %s не является владельцем аккаунта с id = %s",
                        clientId, accountId));
            } else if (account.getBalance().add(transaction.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
                throw new ConflictException("Денег не достаточно для совершения транзакции");
            }

            validatedTransactions.add(transaction);
            account.setBalance(account.getBalance().add(transaction.getAmount()));
            accountRepository.save(account);
        }

        transactionRepository.saveAll(validatedTransactions)
                .stream()
                .map(Transaction::getId)
                .forEach(kafkaTransactionProducer::send);
    }
}
