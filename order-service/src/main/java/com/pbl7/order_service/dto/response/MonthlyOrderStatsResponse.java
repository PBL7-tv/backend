package com.pbl7.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyOrderStatsResponse {
    private int month;
    private int year;
    private long totalOrders;
    private long totalRevenue;
    private long totalItems;
} 