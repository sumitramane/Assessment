package com.finance.repository;

import java.math.BigDecimal;

public interface CategoryTotalProjection {
    String getCategory();
    String getType();
    BigDecimal getTotal();
    Long getCount();
}
