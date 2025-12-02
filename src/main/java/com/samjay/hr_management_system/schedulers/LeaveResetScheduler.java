package com.samjay.hr_management_system.schedulers;

import com.samjay.hr_management_system.enumerations.EmploymentStatus;
import com.samjay.hr_management_system.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.samjay.hr_management_system.constants.Constant.MAX_NUMBER_OF_DAYS_FOR_LEAVE_IN_A_YEAR;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveResetScheduler {

    private final EmployeeRepository employeeRepository;

    @Scheduled(cron = "0 0 0 1 1 *", zone = "Africa/Lagos")
    public void resetAnnualLeave() {

        log.info("Starting annual leave reset job...");

        employeeRepository.findByEmploymentStatusNot(EmploymentStatus.TERMINATED)
                .flatMap(employee -> {

                    employee.setNumberOfLeaveDaysLeft(MAX_NUMBER_OF_DAYS_FOR_LEAVE_IN_A_YEAR);

                    return employeeRepository.save(employee);

                })
                .doOnNext(updatedEmployee -> log.info("Reset leave for employee {}", updatedEmployee.getId()))
                .doOnError(error -> log.error("Error while resetting annual leave", error))
                .doOnComplete(() -> log.info("Annual leave reset job completed successfully."))
                .subscribe();
    }
}
