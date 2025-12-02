package com.samjay.hr_management_system.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.YearMonth;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PayrollMessage {

    private String employeeId;

    private String payrollRunId;

    private YearMonth payrollPeriod;
}
