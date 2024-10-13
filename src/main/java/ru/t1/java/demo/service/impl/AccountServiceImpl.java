package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.aop.Metric;
import ru.t1.java.demo.kafka.KafkaAccountProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repository;
    private final KafkaAccountProducer kafkaAccountProducer;

    @Metric
    @Override
    public void addAccount(List<Account> accounts) {
        repository.saveAll(accounts)
                .stream()
                .map(Account::getId)
                .forEach(kafkaAccountProducer::send);
    }

    @Metric
    @Override
    public List<AccountDto> parseJson() {
        ObjectMapper mapper = new ObjectMapper();

        AccountDto[] accounts = new AccountDto[0];
        try {
            accounts = mapper.readValue(new File("src/main/resources/MOCK_Account_DATA.json"), AccountDto[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Arrays.asList(accounts);
    }
}
