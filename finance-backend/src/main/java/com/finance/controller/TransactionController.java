package com.finance.controller;

import com.finance.dto.request.TransactionRequest;
import com.finance.dto.request.TransactionUpdateRequest;
import com.finance.entity.Transaction.TransactionType;
import com.finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // GET — all authenticated roles
    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(transactionService.findAll(type, category, from, to, page, size));
    }

    // POST — admin only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody TransactionRequest req,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(201).body(transactionService.create(req, userDetails.getUsername()));
    }

    // PUT — admin only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody TransactionUpdateRequest req) {
        return ResponseEntity.ok(transactionService.update(id, req));
    }

    // DELETE (soft) — admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        transactionService.softDelete(id);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted"));
    }
}
