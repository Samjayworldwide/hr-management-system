package com.samjay.hr_management_system.repositories;

import com.samjay.hr_management_system.entities.LeaveRequest;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface LeaveRequestRepository extends R2dbcRepository<LeaveRequest, String> {

    Mono<Boolean> existsByEmployeeEmailAddressAndIsActive(String employeeEmailAddress, boolean isActive);

    Flux<LeaveRequest> findByEmployeeIdAndApprovedDateLessThanEqualAndExpectedReturnDateGreaterThanEqual(String employeeId, LocalDate startDate, LocalDate endDate);

    Flux<LeaveRequest> findAllByIsApproved(boolean isApproved);

    Flux<LeaveRequest> findAllByIsActive(boolean isActive);
}
