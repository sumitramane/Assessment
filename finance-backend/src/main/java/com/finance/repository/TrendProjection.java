package com.finance.repository;

import java.math.BigDecimal;

public interface TrendProjection {
    String getPeriod();
    String getType();
    BigDecimal getTotal();
    Long getCount();
}
