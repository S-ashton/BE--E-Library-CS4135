package com.elibrary.loan_service.mapper;

import com.elibrary.loan_service.domain.Loan;
import com.elibrary.loan_service.dto.LoanDTO;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanDTO toDto(Loan loan) {
         return new LoanDTO(
                loan.getId(),
                loan.getUserId(),
                loan.getBookId(),
                loan.getCopyId(),
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus(),
                loan.getFineAmount()
        );
    }
}