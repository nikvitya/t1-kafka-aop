package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.Metric;
import ru.t1.java.demo.exception.ConflictException;
import ru.t1.java.demo.exception.NotFoundException;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.AccountType;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final KafkaTransactionProducer kafkaTransactionProducer;
    private final AccountService accountService;

    @Metric
    @Override
    @Transactional(readOnly = true)
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

        for (Transaction transaction : transactions) {
            Long clientId = transaction.getClientId();
            Long accountId = transaction.getAccountId();

            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new NotFoundException
                            (String.format("Пользователь с client_id = %, не найден", clientId)));
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new NotFoundException
                            (String.format("Пользователь с account_id = %, не найден", accountId)));

            Boolean isBlocked = account.getIsBlocked();

            if (account.getClientId() != client.getId()) {
                throw new ConflictException(String.format("Клиент с id = %s не является владельцем аккаунта с id = %s",
                        clientId, accountId));
            } else if (account.getType().equals(AccountType.DEBIT)
                    && account.getBalance().add(transaction.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
                throw new ConflictException("Не достаточно средств для совершения транзакции");
            } else if (isBlocked) {
                Transaction wrongTransaction = transactionRepository.save(transaction);
                kafkaTransactionProducer.sendTransaction(wrongTransaction.getId());

            } else if (account.getType().equals(AccountType.CREDIT)
                    && account.getBalance().add(transaction.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
                account.setIsBlocked(true);
            }

            account.setBalance(account.getBalance().add(transaction.getAmount()));
            accountRepository.save(account);

            if (!isBlocked) {
                Transaction savedTransaction = transactionRepository.save(transaction);
                kafkaTransactionProducer.send(savedTransaction.getId());
            }
        }
    }


    @Override
    public void cancelTransaction(List<Long> transactionIds) {
        List<Account> accounts = new ArrayList<>();
        List<Transaction> transactionForRetry = new ArrayList<>();

        for (Long id : transactionIds) {
            Transaction transaction = transactionRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(String.format("Транзакция с id %s не найдена.", id)));
            Long accountId = transaction.getAccountId();
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new NotFoundException(String.format("Аккаунт с id %s не найден", accountId)));

            if (transaction.getAmount().compareTo(BigDecimal.ZERO) >= 0) {
                accountService.unlockAccount(account);

                account.setIsBlocked(false);
                Transaction returnedTransaction = new Transaction(transaction.getAmount(),
                        transaction.getClientId(), transaction.getAccountId(),true,transaction.getType());

                transactionForRetry.add(returnedTransaction);
            }
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            accounts.add(account);
        }

        transactionRepository.deleteAllById(transactionIds);
        accountRepository.saveAll(accounts);
        transactionRepository.saveAll(transactionForRetry);

    }

    @Scheduled(fixedDelayString = "${t1.schedule.transaction.resend.period}")
    public void retryTransaction() {
        List<Transaction> transactionsForRetry = transactionRepository.findByIsNeedRetry(true);

        List<Account> accountsForRetry = new ArrayList<>();
        for (Transaction transactionForRetry : transactionsForRetry) {
            Long accountId = transactionForRetry.getAccountId();
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new NotFoundException(String.format("Аккаунт с id %s не найден", accountId)));
            account.setBalance(account.getBalance().add(transactionForRetry.getAmount()));
            accountsForRetry.add(account);
        }
        accountRepository.saveAll(accountsForRetry);
        transactionsForRetry.forEach(t -> t.setIsNeedRetry(false));
        transactionRepository.saveAll(transactionsForRetry);
    }

    @Override
    public Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId).
                orElseThrow(() -> new NotFoundException(String.format("Транзация с id %s не найдена", transactionId)));

    }
}
