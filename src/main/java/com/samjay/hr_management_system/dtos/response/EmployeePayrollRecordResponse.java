package com.samjay.hr_management_system.dtos.response;

import com.samjay.hr_management_system.enumerations.PayrollRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeePayrollRecordResponse {

    private String payrollMonth;

    private double grossSalary;

    private double netSalary;

    private double deductions;

    private PayrollRecordStatus payrollRecordStatus;
}
