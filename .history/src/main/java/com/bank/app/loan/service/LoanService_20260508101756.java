package com.bank.app.loan.service;

import com.bank.app.customer.entity.Customer;
import com.bank.app.customer.repository.CustomerRepository;
import com.bank.app.exception.NotFoundException;
import com.bank.app.loan.dto.LoanApplicationRequest;
import com.bank.app.loan.entity.Loan;
import com.bank.app.loan.enums.LoanStatus;
import com.bank.app.loan.repository.LoanRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;

    public LoanService(LoanRepository loanRepository, CustomerRepository customerRepository) {
        this.loanRepository = loanRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Loan apply(LoanApplicationRequest request) {
        Customer customer = customerRepository.findByCustomerNumberAndDeletedFalse(request.customerNumber())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setPrincipalAmount(request.principalAmount());
        loan.setInterestRate(request.interestRate());
        loan.setTenorMonths(request.tenorMonths());
        loan.setStatus(LoanStatus.APPLIED);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan approve(@NonNull Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new NotFoundException("Loan not found"));
        loan.setStatus(LoanStatus.APPROVED);
        return loanRepository.save(loan);
    }
}
