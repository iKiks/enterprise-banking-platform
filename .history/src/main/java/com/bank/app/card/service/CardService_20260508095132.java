package com.bank.app.card.service;

import com.bank.app.account.entity.BankAccount;
import com.bank.app.account.repository.BankAccountRepository;
import com.bank.app.card.dto.CardResponse;
import com.bank.app.card.entity.Card;
import com.bank.app.card.repository.CardRepository;
import com.bank.app.customer.entity.Customer;
import com.bank.app.customer.repository.CustomerRepository;
import com.bank.app.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final BankAccountRepository accountRepository;

    public CardService(CardRepository cardRepository, CustomerRepository customerRepository, BankAccountRepository accountRepository) {
        this.cardRepository = cardRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public CardResponse issueCardForCustomer(String customerNumber) {
        Customer customer = customerRepository.findByCustomerNumberAndDeletedFalse(customerNumber)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        Card card = new Card();
        card.setCustomer(customer);
        card.setCardNumber(generateCardNumber());
        card.setCardHolderName(customer.getUser().getFullName());
        card.setActive(true);
        card.setBlocked(false);

        Card saved = cardRepository.save(card);
        return new CardResponse(saved.getCardNumber(), saved.getCardHolderName(), saved.isActive(), saved.isBlocked());
    }

    @Transactional
    public CardResponse issueCardForAccount(String accountNumber) {
        BankAccount account = accountRepository.findByAccountNumberAndDeletedFalse(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Card card = new Card();
        card.setAccount(account);
        card.setPanMasked("5399********" + UUID.randomUUID().toString().substring(0, 4));
        card.setActive(true);
        card.setFrozen(false);

        Card saved = cardRepository.save(card);
        String holder = null;
        if (saved.getAccount() != null && saved.getAccount().getCustomer() != null && saved.getAccount().getCustomer().getUser() != null) {
            holder = saved.getAccount().getCustomer().getUser().getFullName();
        }
        return new CardResponse(saved.getPanMasked(), holder, saved.isActive(), saved.isBlocked());
    }

    @Transactional
    public CardResponse freezeCard(String panMasked) {
        Card card = cardRepository.findByPanMasked(panMasked)
                .orElseThrow(() -> new NotFoundException("Card not found"));
        card.setFrozen(true);
        Card saved = cardRepository.save(card);
        return new CardResponse(saved.getPanMasked(), saved.getCardHolderName(), saved.isActive(), saved.isBlocked());
    }

    private String generateCardNumber() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }
}
