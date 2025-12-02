package com.samjay.hr_management_system.schedulers;

import com.samjay.hr_management_system.dtos.request.PayrollMessage;
import com.samjay.hr_management_system.entities.PayrollRun;
import com.samjay.hr_management_system.enumerations.EmploymentStatus;
import com.samjay.hr_management_system.enumerations.PayrollRunStatus;
import com.samjay.hr_management_system.publishers.PayrollPublisher;
import com.samjay.hr_management_system.repositories.EmployeeRepository;
import com.samjay.hr_management_system.repositories.PayrollRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.YearMonth;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyPayrollScheduler {

    private final EmployeeRepository employeeRepository;

    private final PayrollPublisher payrollPublisher;

    private final PayrollRunRepository payrollRunRepository;

    @Scheduled(cron = "0 0 2 1 * *", zone = "Africa/Lagos")
    public void triggerMonthlyPayroll() {

        String period = YearMonth.now().toString();

        String runId = UUID.randomUUID().toString();

        PayrollRun payrollRun = new PayrollRun();

        payrollRun.setId(runId);

        payrollRun.setRunDate(period);

        payrollRun.setPayrollRunStatus(PayrollRunStatus.RUNNING);

        Mono<PayrollRun> payrollRunMono = payrollRunRepository.save(payrollRun)
                .doOnSuccess(run -> log.info("Initialized PayrollRun {} for period {}", runId, period))
                .doOnError(err -> log.error("Failed to initialize PayrollRun for period {}", period, err));

        payrollRunMono.flatMapMany(run -> employeeRepository.findByEmploymentStatusNot(EmploymentStatus.TERMINATED)
                .flatMap(employee -> {

                    PayrollMessage payrollMessage = new PayrollMessage();

                    payrollMessage.setEmployeeId(employee.getId());

                    payrollMessage.setPayrollRunId(runId);

                    payrollMessage.setPayrollPeriod(YearMonth.parse(period));

                    return payrollPublisher.queuePayroll(payrollMessage)
                            .doOnError(err -> log.error("Failed to queue payroll for emp {}", employee.getId(), err));
                })
                .then(payrollRunRepository.updateStatus(runId, PayrollRunStatus.COMPLETED)
                        .doOnSuccess(v -> log.info("PayrollRun {} COMPLETED", runId))
                )
                .onErrorResume(ex -> {

                    log.error("Payroll failed for period {}", period, ex);

                    return payrollRunRepository.updateStatus(runId, PayrollRunStatus.FAILED)
                            .then(Mono.empty());
                })
        ).subscribe();
    }
}
