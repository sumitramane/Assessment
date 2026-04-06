package com.finance.repository;

import com.finance.entity.Transaction;
import com.finance.entity.Transaction.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpec {

    public static Specification<Transaction> filter(
            TransactionType type, String category, LocalDate from, LocalDate to) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isNull(root.get("deletedAt")));

            if (type != null)     predicates.add(cb.equal(root.get("type"), type));
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (from != null)     predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            if (to != null)       predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
