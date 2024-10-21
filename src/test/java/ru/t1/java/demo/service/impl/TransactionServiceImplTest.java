package ru.t1.java.demo.service.impl;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.t1.java.demo.exception.NotFoundException;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.CheckResponse;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.web.CheckWebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static reactor.core.publisher.Mono.when;


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


    @Test
    void parseJson() {
    }

    @Test
    void addTransaction() {
        // Тестовые данные
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);


        when(checkWebClient.checkTransaction(anyLong())).thenReturn(Optional.of(CheckResponse.builder().blocked(false).build()));

        transactionService.addTransaction(transactions);

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(kafkaTransactionProducer, times(2)).send(anyLong());
    }

    @Test
    void addTransaction_notAllowed() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);

        when(checkWebClient.checkTransaction(transaction.getId())).thenReturn(Optional.of(CheckResponse.builder().blocked(true).build()));

        assertThrows(NotFoundException.class, () -> transactionService.addTransaction(List.of(transaction)));

        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(kafkaTransactionProducer, never()).send(anyLong());
    }


    @Test
    void cancelTransaction() {
    }

    @Test
    void retryTransaction() {
    }

    @Test
    void getTransaction() {
    }
}




