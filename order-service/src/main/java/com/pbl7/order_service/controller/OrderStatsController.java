package com.pbl7.order_service.controller;

import com.pbl7.order_service.dto.ApiResponse;
import com.pbl7.order_service.dto.response.MonthlyOrderStatsResponse;
import com.pbl7.order_service.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderStatsController {

    OrderService orderService;

    @GetMapping("/monthly/{year}")
    public ApiResponse<List<MonthlyOrderStatsResponse>> getMonthlyOrderStats(@PathVariable int year, 
    @RequestHeader("Authorization") String jwt) {
        List<MonthlyOrderStatsResponse> stats = orderService.getMonthlyOrderStats(year);
        return ApiResponse.<List<MonthlyOrderStatsResponse>>builder()
                .result(stats)
                .build();
    }
} 