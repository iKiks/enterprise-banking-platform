package com.bank.app.account.service;

import com.bank.app.account.dto.AccountResponse;
import com.bank.app.account.dto.CreateAccountRequest;
import com.bank.app.account.entity.BankAccount;
import com.bank.app.account.enums.AccountStatus;
import com.bank.app.account.mapper.AccountMapper;
import com.bank.app.account.repository.BankAccountRepository;
import com.bank.app.customer.entity.Customer;
import com.bank.app.customer.repository.CustomerRepository;
import com.bank.app.exception.NotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class AccountService {

    private final BankAccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapper accountMapper;

    public AccountService(BankAccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Customer customer = customerRepository.findByCustomerNumberAndDeletedFalse(request.customerNumber())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        BankAccount account = new BankAccount();
        account.setCustomer(customer);
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(request.accountType());
        account.setCurrencyCode(request.currencyCode().toUpperCase());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setDailyLimit(request.dailyLimit());

        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Cacheable(value = "accounts", key = "#accountNumber")
    @Transactional(readOnly = true)
    public AccountResponse getByAccountNumber(String accountNumber) {
        BankAccount account = accountRepository.findByAccountNumberAndDeletedFalse(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        return accountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse freezeAccount(String accountNumber) {
        BankAccount account = accountRepository.findByAccountNumberAndDeletedFalse(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setStatus(AccountStatus.FROZEN);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse closeAccount(String accountNumber) {
        BankAccount account = accountRepository.findByAccountNumberAndDeletedFalse(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.setStatus(AccountStatus.CLOSED);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    private String generateAccountNumber() {
        long random = 1000000000L + new Random().nextInt(900000000);
        return String.valueOf(random);
    }
}
