package com.finance.controller;

import com.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // Viewer and above
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
    public ResponseEntity<?> summary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    // Analyst and above
    @GetMapping("/category-totals")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<?> categoryTotals() {
        return ResponseEntity.ok(dashboardService.getCategoryTotals());
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<?> trends(@RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(dashboardService.getTrends(period));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
    public ResponseEntity<?> recent() {
        return ResponseEntity.ok(dashboardService.getRecent());
    }
}
