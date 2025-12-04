package com.samjay.hr_management_system.services.implementations;

import com.samjay.hr_management_system.dtos.request.EmailDetails;
import com.samjay.hr_management_system.dtos.request.PayrollMessage;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.EmployeePayrollRecordResponse;
import com.samjay.hr_management_system.dtos.response.PayrollRecordResponse;
import com.samjay.hr_management_system.dtos.response.PayrollResult;
import com.samjay.hr_management_system.entities.Employee;
import com.samjay.hr_management_system.entities.LeaveRequest;
import com.samjay.hr_management_system.entities.PayrollRecord;
import com.samjay.hr_management_system.enumerations.LeaveType;
import com.samjay.hr_management_system.enumerations.PayrollRecordStatus;
import com.samjay.hr_management_system.globalexception.ApplicationException;
import com.samjay.hr_management_system.publishers.EmailPublisher;
import com.samjay.hr_management_system.repositories.EmployeeRepository;
import com.samjay.hr_management_system.repositories.LeaveRequestRepository;
import com.samjay.hr_management_system.repositories.PayrollRecordRepository;
import com.samjay.hr_management_system.services.PayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.YearMonth;
import java.util.List;

import static com.samjay.hr_management_system.constants.Constant.TOTAL_NUMBER_OF_WORKING_DAYS_IN_A_MONTH;
import static com.samjay.hr_management_system.utils.Utility.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollServiceImplementation implements PayrollService {

    private final EmployeeRepository employeeRepository;

    private final LeaveRequestRepository leaveRequestRepository;

    private final PayrollRecordRepository payrollRecordRepository;

    private final EmailPublisher emailPublisher;

    @Override
    public Mono<Void> processPayroll(PayrollMessage payrollMessage) {

        return employeeRepository.findById(payrollMessage.getEmployeeId())
                .flatMap(employee -> calculatePayroll(payrollMessage, employee)
                        .flatMap(payrollResult -> {

                            PayrollRecord payrollRecord = mapToPayrollRecordFromPayrollResult(payrollResult, payrollMessage);

                            double balance = employee.getWalletBalance();

                            double newBalance = balance + payrollResult.getNetPay();

                            employee.setWalletBalance(newBalance);

                            return payrollRecordRepository.save(payrollRecord)
                                    .then(employeeRepository.save(employee))
                                    .doOnSuccess(ignored -> {

                                        EmailDetails emailDetails = EmailDetails
                                                .builder()
                                                .recipient(employee.getPersonalEmailAddress())
                                                .subject("PAYROLL PROCESSED FOR " + payrollResult.getPayrollPeriod().toString())
                                                .messageBody(getPayslipEmail(employee.getFullName(),
                                                        payrollResult.getPayrollPeriod().getMonth().toString(),
                                                        payrollResult.getPayrollPeriod().getYear(), payrollResult.getNetPay()))
                                                .build();

                                        emailPublisher.queueEmail(emailDetails)
                                                .doOnError(error -> log.warn("Failed to queue payslip email: {}", error.getMessage()))
                                                .onErrorComplete()
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .subscribe();
                                    });
                        }).onErrorResume(error -> {

                            log.error("Payroll FAILED for employeeId: {}", payrollMessage.getEmployeeId(), error);

                            PayrollRecord failedRecord = new PayrollRecord();

                            failedRecord.setEmployeeId(employee.getId());

                            failedRecord.setPayrollPeriod(payrollMessage.getPayrollPeriod().toString());

                            failedRecord.setPayrollRecordStatus(PayrollRecordStatus.FAILED);

                            return payrollRecordRepository.save(failedRecord)
                                    .then(Mono.empty());
                        })
                )
                .switchIfEmpty(Mono.empty()).then();
    }

    @Override
    public Mono<ApiResponse<List<PayrollRecordResponse>>> getAllEmployeePayrollRecordsForAMonth(YearMonth yearMonth) {

        return payrollRecordRepository.findAllByPayrollPeriod(yearMonth.toString())
                .flatMap(payrollRecord -> employeeRepository.findById(payrollRecord.getEmployeeId())
                        .map(employee -> mapToPayrollRecordResponseFromPayrollRecord(payrollRecord, employee))
                        .switchIfEmpty(Mono.error(new ApplicationException("Could not find employee with provided identifier")))
                )
                .collectList()
                .flatMap(payrollRecordResponses -> {

                    if (payrollRecordResponses.isEmpty())
                        return Mono.just(ApiResponse.<List<PayrollRecordResponse>>error("Could not find any payroll record for the given period"));

                    return Mono.just(ApiResponse.success("Payroll records fetched successfully", payrollRecordResponses));

                })
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred fetching all payroll records {}", error.getMessage());

                    return Mono.just(ApiResponse.error("Could not fetch payroll records for all employees"));
                });
    }

    @Override
    public Mono<ApiResponse<EmployeePayrollRecordResponse>> getAnEmployeePayrollRecordsForAMonth(YearMonth yearMonth) {

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {

                    String workEmailAddress = securityContext.getAuthentication().getName();

                    return employeeRepository.findByWorkEmailAddressIgnoreCase(workEmailAddress)
                            .flatMap(employee -> payrollRecordRepository.findByEmployeeIdAndPayrollPeriod(employee.getId(), yearMonth.toString())
                                    .map(payrollRecord -> {

                                        EmployeePayrollRecordResponse employeePayrollRecordResponse = mapToEmployeePayrollRecordResponseFromPayrollRecord(payrollRecord);

                                        return ApiResponse.success("Payroll record retrieved successfully", employeePayrollRecordResponse);

                                    })
                                    .switchIfEmpty(Mono.just(ApiResponse.error("Could not find any payroll record for given period")))
                            )
                            .switchIfEmpty(Mono.just(ApiResponse.error("Could not find employee with given email")));
                })
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred {}", error.getMessage());

                    return Mono.just(ApiResponse.error("An unexpected error occurred fetching your payslip"));

                });
    }

    private Mono<PayrollResult> calculatePayroll(PayrollMessage payrollMessage, Employee employee) {

        return leaveRequestRepository.findByEmployeeIdAndApprovedDateLessThanEqualAndExpectedReturnDateGreaterThanEqual(payrollMessage.getEmployeeId(),
                        payrollMessage.getPayrollPeriod().atDay(1),
                        payrollMessage.getPayrollPeriod().atEndOfMonth())
                .filter(leaveRequest -> leaveRequest.getLeaveType() == LeaveType.UNPAID_LEAVE)
                .map(LeaveRequest::getNumberOfLeaveDays)
                .reduce(0, Integer::sum)
                .map(unpaidLeaveDays -> {

                    double dailySalary = employee.getSalary() / TOTAL_NUMBER_OF_WORKING_DAYS_IN_A_MONTH;

                    double deduction = dailySalary * unpaidLeaveDays;

                    double netSalary = employee.getSalary() - deduction;

                    PayrollResult payrollResult = new PayrollResult();

                    payrollResult.setEmployeeId(employee.getId());

                    payrollResult.setPayrollPeriod(payrollMessage.getPayrollPeriod());

                    payrollResult.setGrossPay(employee.getSalary());

                    payrollResult.setDeductions(deduction);

                    payrollResult.setNetPay(netSalary);

                    return payrollResult;
                });
    }
}
