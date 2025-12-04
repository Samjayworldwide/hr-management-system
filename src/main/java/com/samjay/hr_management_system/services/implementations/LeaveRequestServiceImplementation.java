package com.samjay.hr_management_system.services.implementations;

import com.samjay.hr_management_system.dtos.request.CreateLeaveRequest;
import com.samjay.hr_management_system.dtos.request.EmailDetails;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.ActiveLeaveResponse;
import com.samjay.hr_management_system.dtos.response.LeaveResponse;
import com.samjay.hr_management_system.entities.LeaveRequest;
import com.samjay.hr_management_system.enumerations.EmploymentStatus;
import com.samjay.hr_management_system.enumerations.GrantLeaveAuthority;
import com.samjay.hr_management_system.publishers.EmailPublisher;
import com.samjay.hr_management_system.repositories.EmployeeRepository;
import com.samjay.hr_management_system.repositories.LeaveRequestRepository;
import com.samjay.hr_management_system.services.LeaveRequestService;
import com.samjay.hr_management_system.utils.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.List;

import static com.samjay.hr_management_system.constants.Constant.MAX_NUMBER_OF_DAYS_FOR_LEAVE_IN_A_YEAR;
import static com.samjay.hr_management_system.utils.Utility.mapToLeaveRequestFromCreateLeaveRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestServiceImplementation implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    private final EmployeeRepository employeeRepository;

    private final EmailPublisher emailPublisher;

    @Override
    public Mono<ApiResponse<String>> submitLeaveRequest(Mono<CreateLeaveRequest> createLeaveRequestMono) {

        return createLeaveRequestMono.flatMap(createLeaveRequest -> ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {

                    String workEmailAddress = securityContext.getAuthentication().getName();

                    return employeeRepository.findByWorkEmailAddressIgnoreCase(workEmailAddress)
                            .flatMap(employee -> {

                                if (createLeaveRequest.getNumberOfLeaveDays() > MAX_NUMBER_OF_DAYS_FOR_LEAVE_IN_A_YEAR)
                                    return Mono.just(ApiResponse.<String>error("You cannot exceed the max number of leave days in a year"));

                                if (employee.getProfileCompletion() != 100)
                                    return Mono.just(ApiResponse.<String>error("Profile has not been completed. Please try to complete your profile to submit leave request"));

                                if (employee.getNumberOfLeaveDaysLeft() - createLeaveRequest.getNumberOfLeaveDays() < 0)
                                    return Mono.just(ApiResponse.<String>error("You have exceeded the number of leave days you have left"));

                                return leaveRequestRepository.existsByEmployeeEmailAddressAndIsActive(employee.getWorkEmailAddress(), true)
                                        .flatMap(leaveExists -> {

                                            List<String> roles = securityContext
                                                    .getAuthentication()
                                                    .getAuthorities()
                                                    .stream()
                                                    .map(GrantedAuthority::getAuthority)
                                                    .toList();

                                            GrantLeaveAuthority grantLeaveAuthority = roles.contains("HR_ROLE") ? GrantLeaveAuthority.SHOULD_BE_GRANTED_BY_ADMIN : GrantLeaveAuthority.SHOULD_BE_GRANTED_BY_HR;

                                            if (leaveExists)
                                                return Mono.just(ApiResponse.<String>error("You already have an active leave request"));

                                            LeaveRequest leaveRequest = mapToLeaveRequestFromCreateLeaveRequest(createLeaveRequest, employee, workEmailAddress, grantLeaveAuthority);

                                            return leaveRequestRepository.save(leaveRequest)
                                                    .thenReturn(ApiResponse.<String>success("Leave request has been created successfully, you will be informed on the outcome of your request"));
                                        });
                            })
                            .switchIfEmpty(Mono.just(ApiResponse.error("Employee not found with given email")));
                })

        ).onErrorResume(error -> {

            log.error("An unexpected error occurred submitting leave request {} ", error.getMessage());

            return Mono.just(ApiResponse.error("Failed to submit leave request"));

        });
    }

    @Override
    public Mono<ApiResponse<String>> approveLeaveRequest(String id) {

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> leaveRequestRepository.findById(id)
                        .flatMap(leaveRequest -> employeeRepository.findById(leaveRequest.getEmployeeId())
                                .flatMap(employee -> {

                                    if (leaveRequest.isApproved())
                                        return Mono.just(ApiResponse.<String>error("Leave request is already approved"));

                                    List<String> roles = securityContext
                                            .getAuthentication()
                                            .getAuthorities()
                                            .stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .toList();

                                    String email = securityContext.getAuthentication().getName();

                                    if (leaveRequest.getGrantLeaveAuthority() == GrantLeaveAuthority.SHOULD_BE_GRANTED_BY_ADMIN && !roles.contains("ADMIN_ROLE"))
                                        return Mono.just(ApiResponse.<String>error("Please you cannot approve this leave request because you are not an admin"));

                                    LocalDate expectedReturnDate = LocalDate.now().plusDays(leaveRequest.getNumberOfLeaveDays());

                                    leaveRequest.setApproved(true);

                                    leaveRequest.setActive(true);

                                    leaveRequest.setApprovedBy(email);

                                    leaveRequest.setApprovedDate(LocalDate.now());

                                    leaveRequest.setExpectedReturnDate(expectedReturnDate);

                                    int numberOfLeaveDaysLeft = employee.getNumberOfLeaveDaysLeft() - leaveRequest.getNumberOfLeaveDays();

                                    employee.setNumberOfLeaveDaysLeft(numberOfLeaveDaysLeft);

                                    employee.setEmploymentStatus(EmploymentStatus.ON_LEAVE);

                                    return leaveRequestRepository.save(leaveRequest)
                                            .then(employeeRepository.save(employee))
                                            .doOnSuccess(ignored -> {

                                                EmailDetails emailDetails = EmailDetails
                                                        .builder()
                                                        .messageBody(Utility.getLeaveRequestApprovedEmail(employee.getFirstname(), expectedReturnDate.toString()))
                                                        .recipient(employee.getPersonalEmailAddress())
                                                        .subject("LEAVE APPROVAL")
                                                        .build();

                                                emailPublisher.queueEmail(emailDetails)
                                                        .doOnError(error -> log.warn("Failed to queue onboarding email: {}", error.getMessage()))
                                                        .onErrorComplete()
                                                        .subscribeOn(Schedulers.boundedElastic())
                                                        .subscribe();
                                            })
                                            .thenReturn(ApiResponse.<String>success("Leave has been approved successfully"));
                                })
                                .switchIfEmpty(Mono.just(ApiResponse.error("Employee with given identifier not found")))
                        )
                        .switchIfEmpty(Mono.just(ApiResponse.error("Could not find any leave request with the given id")))

                )
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred approving leave request {}", error.getMessage());

                    return Mono.just(ApiResponse.error("Failed to approve leave request"));

                });
    }

    @Override
    public Mono<ApiResponse<List<LeaveResponse>>> getAllUnapprovedLeaveRequests() {

        return leaveRequestRepository.findAllByIsApproved(false)
                .map(Utility::mapToLeaveResponseFromLeaveRequest)
                .collectList()
                .map(leaveResponses -> {

                    if (leaveResponses.isEmpty())
                        return ApiResponse.<List<LeaveResponse>>error("No pending leave requests found");

                    else
                        return ApiResponse.success("Leave requests retrieved successfully", leaveResponses);
                })
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred retrieving leave requests {}", error.getMessage());

                    return Mono.just(ApiResponse.error("Failed to retrieve leave requests"));

                });
    }

    @Override
    public Mono<ApiResponse<List<ActiveLeaveResponse>>> getAllEmployeesCurrentlyOnLeave() {

        return leaveRequestRepository.findAllByIsActive(true)
                .map(Utility::mapToActiveLeaveResponseFromEmployeeAndLeaveRequest)
                .collectList()
                .map(leaveResponses -> {

                    if (leaveResponses.isEmpty())
                        return ApiResponse.<List<ActiveLeaveResponse>>error("No employees are currently on leave");

                    else
                        return ApiResponse.success("Employees currently on leave retrieved successfully", leaveResponses);
                })
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred retrieving employees currently on leave {}", error.getMessage());

                    return Mono.just(ApiResponse.error("Failed to retrieve employees currently on leave"));

                });
    }
}
