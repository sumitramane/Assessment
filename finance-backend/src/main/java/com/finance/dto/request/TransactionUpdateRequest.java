package com.finance.dto.request;

import com.finance.entity.Transaction.TransactionType;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/** All fields optional — only non-null values are applied to the existing record. */
@Data
public class TransactionUpdateRequest {

    @Positive(message = "Amount must be a positive number")
    private BigDecimal amount;

    private TransactionType type;
    private String category;
    private LocalDate date;
    private String notes;
}
