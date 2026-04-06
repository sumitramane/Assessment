package com.finance.dto.response;

import com.finance.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private String type;
    private String category;
    private LocalDate date;
    private String notes;
    private Long createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getType().name(),
                t.getCategory(),
                t.getDate(),
                t.getNotes(),
                t.getCreatedBy().getId(),
                t.getCreatedBy().getUsername(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
