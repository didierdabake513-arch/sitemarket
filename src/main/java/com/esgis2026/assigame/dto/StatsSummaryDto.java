package com.esgis2026.assigame.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatsSummaryDto {
    private long totalProducts;
    private long totalOrders;
    private long totalUsers;
    private double totalRevenue;
}
