package com.finance.service;

import com.finance.dto.request.TransactionRequest;
import com.finance.dto.request.TransactionUpdateRequest;
import com.finance.dto.response.TransactionResponse;
import com.finance.entity.Transaction;
import com.finance.entity.Transaction.TransactionType;
import com.finance.entity.User;
import com.finance.repository.TransactionRepository;
import com.finance.repository.TransactionSpec;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public Page<TransactionResponse> findAll(
            TransactionType type, String category, LocalDate from, LocalDate to, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("date").descending());
        return transactionRepository
                .findAll(TransactionSpec.filter(type, category, from, to), pageable)
                .map(TransactionResponse::from);
    }

    public TransactionResponse create(TransactionRequest req, String creatorEmail) {
        User user = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Transaction tx = transactionRepository.save(Transaction.builder()
                .amount(req.getAmount())
                .type(req.getType())
                .category(req.getCategory())
                .date(req.getDate())
                .notes(req.getNotes())
                .createdBy(user)
                .build());

        return TransactionResponse.from(tx);
    }

    public TransactionResponse update(Long id, TransactionUpdateRequest req) {
        Transaction tx = transactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (req.getAmount()   != null) tx.setAmount(req.getAmount());
        if (req.getType()     != null) tx.setType(req.getType());
        if (req.getCategory() != null) tx.setCategory(req.getCategory());
        if (req.getDate()     != null) tx.setDate(req.getDate());
        if (req.getNotes()    != null) tx.setNotes(req.getNotes());

        return TransactionResponse.from(transactionRepository.save(tx));
    }

    public void softDelete(Long id) {
        Transaction tx = transactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        tx.setDeletedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }
}
