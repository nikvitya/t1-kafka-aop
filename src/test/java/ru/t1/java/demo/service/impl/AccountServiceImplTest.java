package ru.t1.java.demo.service.impl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.exception.NotFoundException;
import ru.t1.java.demo.kafka.KafkaAccountProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.AccountType;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @InjectMocks
    @Spy
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private KafkaAccountProducer kafkaAccountProducer;

    @Test
    void addAccount() {
        Account account1 = new Account();
        account1.setId(1L);
        Account account2 = new Account();
        account2.setId(2L);
        List<Account> accounts = Arrays.asList(account1, account2);

        when(accountRepository.saveAll(accounts)).thenReturn(accounts);
        accountService.addAccount(accounts);

        verify(kafkaAccountProducer, times(2)).send(anyLong());
        verify(accountRepository,times(1)).saveAll(accounts);
    }

    @Test
    void registerAccount() {
        Account account = new Account();
        account.setId(1L);

        when(accountRepository.save(account)).thenReturn(account);

        Account registeredAccount = accountService.registerAccount(account);

        assertEquals(account, registeredAccount);
    }

    @Test
    void parseJson() {

        List<AccountDto> expectedAccountDtos = List.of(
                AccountDto.builder()
                        .type(AccountType.DEBIT)
                        .build()
        );
        when(accountService.parseJson()).thenReturn(expectedAccountDtos);
        List<AccountDto> actualAccountDtos = accountService.parseJson();

        assertEquals(expectedAccountDtos.get(0).getType(), actualAccountDtos.get(0).getType());
    }

    @Test
    void blockAccount() {
        Long accountId = 1L;
        Account account = new Account();
        account.setId(accountId);
        account.setType(AccountType.DEBIT);
        account.setIsBlocked(false);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account blockedAccount = accountService.blockAccount(accountId);

        assertTrue(blockedAccount.getIsBlocked());
    }

    @Test
    void findById() {
        Long accountId = 1L;
        Account account = new Account();
        account.setId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Account foundAccount = accountService.findById(accountId);

        assertEquals(account, foundAccount);
    }

    @Test
    void findById_notExistAccount() {
        Long accountId = 12L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> accountService.findById(accountId));

        assertEquals("Аккаунт с id = 12 не найден", exception.getMessage());
    }

    @Test
    void unlockAccount() {
        Account account = new Account();
        account.setBalance(new BigDecimal("100.00"));
        account.setIsBlocked(true);

        accountService.unlockAccount(account);

        assertFalse(account.getIsBlocked());
    }
}