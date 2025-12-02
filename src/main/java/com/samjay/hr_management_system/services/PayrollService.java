package com.samjay.hr_management_system.services;

import com.samjay.hr_management_system.dtos.request.PayrollMessage;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.EmployeePayrollRecordResponse;
import com.samjay.hr_management_system.dtos.response.PayrollRecordResponse;
import reactor.core.publisher.Mono;

import java.time.YearMonth;
import java.util.List;

public interface PayrollService {

    Mono<Void> processPayroll(PayrollMessage payrollMessage);

    Mono<ApiResponse<List<PayrollRecordResponse>>> getAllEmployeePayrollRecordsForAMonth(YearMonth yearMonth);

    Mono<ApiResponse<EmployeePayrollRecordResponse>> getAnEmployeePayrollRecordsForAMonth(YearMonth yearMonth);

}
