package com.samjay.hr_management_system.repositories;

import com.samjay.hr_management_system.entities.PayrollRecord;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface PayrollRecordRepository extends R2dbcRepository<PayrollRecord, String> {

    Flux<PayrollRecord> findAllByPayrollPeriod(String payrollPeriod);

    Mono<PayrollRecord> findByEmployeeIdAndPayrollPeriod(String employeeId, String payrollPeriod);
}
