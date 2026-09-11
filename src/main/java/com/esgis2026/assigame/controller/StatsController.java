package com.esgis2026.assigame.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esgis2026.assigame.dto.CategorySalesDto;
import com.esgis2026.assigame.dto.MonthlyRevenueDto;
import com.esgis2026.assigame.dto.StatsSummaryDto;
import com.esgis2026.assigame.service.StatsService;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public StatsSummaryDto getSummary() {
        return statsService.getSummary();
    }

    @GetMapping("/revenue-monthly")
    public List<MonthlyRevenueDto> getRevenueMonthly(@RequestParam(defaultValue = "2025") int year) {
        return statsService.getRevenueMonthly(year);
    }

    @GetMapping("/sales-by-category")
    public List<CategorySalesDto> getSalesByCategory() {
        return statsService.getSalesByCategory();
    }
}
