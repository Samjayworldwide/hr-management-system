package com.samjay.hr_management_system.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.YearMonth;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PayrollResult {

    private String employeeId;

    private String payrollRunId;

    private YearMonth payrollPeriod;

    private double grossPay;

    private double netPay;

    private double deductions;
}
