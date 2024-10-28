package ru.t1.java.demo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.impl.TransactionServiceImpl;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource("classpath:application-test.yml")
public class TransactionServiceIntegrationTest {
    @Autowired
    TransactionServiceImpl transactionService;

    @Test
    void isTransactionAllowedTest() {

        Transaction transaction = new Transaction();
        transaction.setId(1L);

        boolean isAllowed = transactionService.isTransactionAllowed(transaction);
        assertTrue(isAllowed);
    }
}

/*
1. Задействовать веб-клиент из проекта.
2. Реализовать wiremock-заглушку, которая отсылает ответ о том, разрешено ли проведение транзакции или нет.
3. Дополнить сервис логикой, которая отсылает запрос на разрешение транзакции в заглушку.
4. Покрыть логику запроса интеграционным тестом
 */

