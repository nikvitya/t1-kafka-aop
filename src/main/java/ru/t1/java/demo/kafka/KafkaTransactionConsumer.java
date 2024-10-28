package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.mapper.TransactionMapper;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionConsumer {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @KafkaListener(id = "${t1.kafka.consumer.transaction-id}",
            topics = "${t1.kafka.topic.transaction_registration}",
            containerFactory = "kafkaTransactionListenerContainerFactory")
    public void listener(@Payload List<TransactionDto> messageList,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("Transaction consumer: Обработка новых сообщений");

        try {
            List<Transaction> transactions = messageList.stream()
                    .map(dto -> transactionMapper.toEntity(dto))
                    .toList();
            transactionService.addTransaction(transactions);
        } finally {
            ack.acknowledge();
        }

        log.debug("Transaction consumer: записи обработаны");
    }

    @KafkaListener(id = "${t1.kafka.consumer.transaction-error-id}",
            topics = "${t1.kafka.topic.transaction_errors}",
            containerFactory = "kafkaTransactionErrorListenerContainerFactory")
    public void errorListener(@Payload List<String> messageList,
                              Acknowledgment ack,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                              @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("Transaction consumer: Обработка новых сообщений");

        try {
            transactionService.cancelTransaction(messageList.stream().map(Long::parseLong).toList());
        } finally {
            ack.acknowledge();
        }

        log.debug("Transaction consumer: записи обработаны");
    }




}
