package com.bank.app.card.controller;

import com.bank.app.card.dto.CardResponse;
import com.bank.app.card.service.CardService;
import com.bank.app.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/issue/customer/{customerNumber}")
    @PreAuthorize("hasAnyRole('TELLER','BANKER','MANAGER','ADMIN')")
    public ApiResponse<CardResponse> issueForCustomer(@PathVariable String customerNumber) {
        return ApiResponse.success("Card issued", cardService.issueCardForCustomer(customerNumber));
    }

    @PostMapping("/issue/account/{accountNumber}")
    @PreAuthorize("hasAnyRole('BANKER','MANAGER','ADMIN')")
    public ApiResponse<CardResponse> issueForAccount(@PathVariable String accountNumber) {
        return ApiResponse.success("Card issued", cardService.issueCardForAccount(accountNumber));
    }

    @PostMapping("/{panMasked}/freeze")
    @PreAuthorize("hasAnyRole('CUSTOMER','BANKER','MANAGER','ADMIN')")
    public ApiResponse<CardResponse> freeze(@PathVariable String panMasked) {
        return ApiResponse.success("Card frozen", cardService.freezeCard(panMasked));
    }
}
