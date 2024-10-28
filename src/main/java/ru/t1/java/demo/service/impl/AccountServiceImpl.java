package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.Metric;
import ru.t1.java.demo.exception.NotFoundException;
import ru.t1.java.demo.kafka.KafkaAccountProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.AccountType;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final KafkaAccountProducer kafkaAccountProducer;
    private final AccountRepository accountRepository;

    @Metric
    @Override
    public void addAccount(List<Account> accounts) {
        accountRepository.saveAll(accounts)
                .stream()
                .map(Account::getId)
                .forEach(kafkaAccountProducer::send);
    }

    @Override
    public Account registerAccount(Account account) {
        return accountRepository.save(account);
    }

    @Metric
    @Override
    @Transactional(readOnly = true)
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

    @Override
    public Account blockAccount(Long accountId) {
        Account account = findById(accountId);
        if (account.getType().equals(AccountType.DEBIT)) {
            account.setIsBlocked(true);
        }

        return accountRepository.save(account);
    }

    public Account findById(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException(String.format("Аккаунт с id =% не найден", accountId)));
    }

    @Override
    public void unlockAccount(Account account) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) >= 0) {
            account.setIsBlocked(false);
        } else {
            log.info("Аккаунт с отрицательным балансом не может быть разблокирован");
        }
    }






}
