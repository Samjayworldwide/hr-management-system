package com.samjay.hr_management_system.repositories;

import com.samjay.hr_management_system.entities.PayrollRun;
import com.samjay.hr_management_system.enumerations.PayrollRunStatus;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PayrollRunRepository extends R2dbcRepository<PayrollRun, String> {

    @Modifying
    @Query("UPDATE payroll_runs SET payroll_run_status = :status WHERE id = :id")
    Mono<Void> updateStatus(String id, PayrollRunStatus status);

}
