package com.medicare.dto;

import lombok.Data;

import java.util.List;

@Data
public class DashboardStatsDto {
    private Long totalDoctors;
    private Long totalPatients;
    private Long totalAppointments;
    private Long todaysAppointments;
    private Double totalRevenue;
    
    // Chart.js Data
    private List<String> monthlyLabels;
    private List<Long> monthlyBookings;
    private List<Double> monthlyRevenue;
    
    private List<String> departmentLabels;
    private List<Long> departmentBookings;
}
