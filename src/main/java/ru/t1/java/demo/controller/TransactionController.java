package ru.t1.java.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LogException;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.mapper.TransactionMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.model.dto.AccountFullDto;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.model.dto.TransactionFullDto;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final KafkaTransactionProducer kafkaTransactionProducer;

    @Value("${t1.kafka.topic.transaction_registration}")
    private String topic;

    @LogException
    @Track
    @GetMapping(value = "/parseTransaction")
    @HandlingResult
    public void parseSource() {
        List<TransactionDto> transactionDtos = transactionService.parseJson();
        transactionDtos.forEach(dto -> {
            kafkaTransactionProducer.sendTo(topic, dto);
        });
    }

    @PostMapping("/transaction/add")
    public void addTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        kafkaTransactionProducer.sendTo(topic, transactionDto);
    }

    @GetMapping("/transaction/{id}")
    public TransactionFullDto getTransaction(@PathVariable Long id) {
        return transactionMapper.toFullDto(transactionService.getTransaction(id));
    }

}
