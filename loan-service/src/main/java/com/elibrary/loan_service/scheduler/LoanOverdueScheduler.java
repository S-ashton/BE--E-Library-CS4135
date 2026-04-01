package com.elibrary.loan_service.scheduler;

import com.elibrary.loan_service.service.LoanService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoanOverdueScheduler {

    private final LoanService loanService;

    public LoanOverdueScheduler(LoanService loanService) {
        this.loanService = loanService;
    }

    @Scheduled(fixedRate = 60000)
    public void scanForOverdueLoans() {
        loanService.processOverdueLoans();
    }
}