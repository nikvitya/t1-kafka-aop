package ru.t1.java.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LogException;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.kafka.KafkaAccountProducer;
import ru.t1.java.demo.mapper.AccountMapper;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.model.dto.AccountFullDto;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final KafkaAccountProducer kafkaAccountProducer;
    @Value("${t1.kafka.topic.account_registration}")
    private String topic;

    @LogException
    @Track
    @GetMapping(value = "/parseAccount")
    @HandlingResult
    public void parseSource() {
        List<AccountDto> accountDtos = accountService.parseJson();
        accountDtos.forEach(dto -> {
            kafkaAccountProducer.sendTo(topic, dto);
        });
    }

    @PostMapping("/account/register")
    public AccountFullDto registerAccount(@Valid @RequestBody AccountDto accountDto) {
        Account account= accountService.registerAccount(accountMapper.toEntity(accountDto));

        return accountMapper.toFullDto(account);
    }

    @GetMapping("/account/block/{accountId}")
    public AccountFullDto blockAccount(@PathVariable Long accountId) {
        Account account = accountService.blockAccount(accountId);

        return accountMapper.toFullDto(account);
    }




}
