package ru.t1.java.demo.service.impl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.exception.ConflictException;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.AccountType;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.CheckResponse;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.web.CheckWebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CheckWebClient checkWebClient;
    @Mock
    private KafkaTransactionProducer kafkaTransactionProducer;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private AccountRepository accountRepository;


    @Test
    void parseJson() {
    }

    @Test
    void addTransaction_Allowed() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setClientId(1L);
        transaction.setAccountId(2L);
        transaction.setAmount(new BigDecimal("100"));

        Account account = new Account();
        account.setBalance(new BigDecimal("500"));
        account.setClientId(1L);
        account.setType(AccountType.CREDIT);
        account.setIsBlocked(false);

        Client client = new Client();
        client.setId(1L);

        when(checkWebClient.checkTransaction(anyLong())).
                thenReturn(Optional.of(CheckResponse.builder().blocked(false).build()));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        transactionService.addTransaction(List.of(transaction));

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(kafkaTransactionProducer, times(1)).send(anyLong());
    }

    @Test
    void addTransaction_notAllowed() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setClientId(1L);
        transaction.setAccountId(2L);
        transaction.setAmount(new BigDecimal("100"));

        Account account = new Account();
        account.setBalance(new BigDecimal("500"));
        account.setClientId(1L);
        account.setType(AccountType.CREDIT);
        account.setIsBlocked(false);

        Client client = new Client();
        client.setId(1L);

        when(checkWebClient.checkTransaction(anyLong())).
                thenReturn(Optional.of(CheckResponse.builder().blocked(true).build()));

        assertThrows(ConflictException.class, () -> transactionService.addTransaction(List.of(transaction)));

        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(kafkaTransactionProducer, times(0)).send(anyLong());
    }


    @Test
    void cancelTransaction() {

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setClientId(1L);
        transaction.setAccountId(2L);
        transaction.setAmount(new BigDecimal("100"));

        Account account = new Account();
        account.setId(2L);
        account.setBalance(new BigDecimal("500"));
        account.setClientId(1L);
        account.setType(AccountType.CREDIT);
        account.setIsBlocked(false);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(account));

        transactionService.cancelTransaction(List.of(1L));

        verify(transactionRepository, times(1)).deleteAllById(List.of(1L));
        verify(accountRepository, times(1)).saveAll(anyList());
    }

    @Test
    void retryTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setClientId(1L);
        transaction.setAccountId(2L);
        transaction.setAmount(new BigDecimal("100"));
        transaction.setIsNeedRetry(true);

        Account account = new Account();
        account.setId(2L);
        account.setBalance(new BigDecimal("500"));
        account.setClientId(1L);
        account.setType(AccountType.CREDIT);
        account.setIsBlocked(false);

        when(transactionRepository.findByIsNeedRetry(true)).thenReturn(List.of(transaction));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(account));

        transactionService.retryTransaction();

        verify(accountRepository, times(1)).saveAll(anyList());
        verify(transactionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Transaction result = transactionService.getTransaction(1L);

        assertNotNull(result);
    }
}




