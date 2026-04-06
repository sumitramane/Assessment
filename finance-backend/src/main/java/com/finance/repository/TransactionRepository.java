package com.finance.repository;

import com.finance.entity.Transaction;
import com.finance.entity.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.deletedAt IS NULL")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.deletedAt IS NULL")
    long countActive();

    @Query(value = """
            SELECT category, type, SUM(amount) AS total, COUNT(*) AS count
            FROM transactions
            WHERE deleted_at IS NULL
            GROUP BY category, type
            ORDER BY total DESC
            """, nativeQuery = true)
    List<CategoryTotalProjection> getCategoryTotals();

    @Query(value = """
            SELECT DATE_FORMAT(date, :fmt) AS period, type, SUM(amount) AS total, COUNT(*) AS count
            FROM transactions
            WHERE deleted_at IS NULL
            GROUP BY period, type
            ORDER BY period DESC
            LIMIT 24
            """, nativeQuery = true)
    List<TrendProjection> getTrends(@Param("fmt") String fmt);

    @Query("SELECT t FROM Transaction t WHERE t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    Page<Transaction> findRecent(Pageable pageable);
}
