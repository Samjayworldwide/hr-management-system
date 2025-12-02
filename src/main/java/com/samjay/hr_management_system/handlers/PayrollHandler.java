package com.samjay.hr_management_system.handlers;

import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.EmployeePayrollRecordResponse;
import com.samjay.hr_management_system.dtos.response.PayrollRecordResponse;
import com.samjay.hr_management_system.services.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PayrollHandler {

    private final PayrollService payrollService;

    public Mono<ServerResponse> getAllEmployeePayrollRecordsForAMonthHandler(ServerRequest serverRequest) {

        YearMonth payrollPeriod = YearMonth.parse(serverRequest.pathVariable("payrollPeriod"));

        Mono<ApiResponse<List<PayrollRecordResponse>>> apiResponseMono = payrollService.getAllEmployeePayrollRecordsForAMonth(payrollPeriod);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> getAnEmployeePayrollRecordsForAMonthHandler(ServerRequest serverRequest) {

        YearMonth payrollPeriod = YearMonth.parse(serverRequest.pathVariable("payrollPeriod"));

        Mono<ApiResponse<EmployeePayrollRecordResponse>> apiResponseMono = payrollService.getAnEmployeePayrollRecordsForAMonth(payrollPeriod);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }
}
