package com.samjay.hr_management_system.leaverequests;

import com.samjay.hr_management_system.dtos.request.CreateLeaveRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.entities.Employee;
import com.samjay.hr_management_system.publishers.EmailPublisher;
import com.samjay.hr_management_system.repositories.EmployeeRepository;
import com.samjay.hr_management_system.repositories.LeaveRequestRepository;
import com.samjay.hr_management_system.services.implementations.LeaveRequestServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LeaveRequestServiceImplementationTests {

    private LeaveRequestRepository leaveRepo;
    private EmployeeRepository employeeRepo;

    private LeaveRequestServiceImplementation service;

    @BeforeEach
    void setup() {

        leaveRepo = mock(LeaveRequestRepository.class);
        employeeRepo = mock(EmployeeRepository.class);
        EmailPublisher emailPublisher = mock(EmailPublisher.class);
        when(emailPublisher.queueEmail(any())).thenReturn(Mono.empty());

        service = new LeaveRequestServiceImplementation(leaveRepo, employeeRepo, emailPublisher);
    }

    private Employee mockEmployee(String id) {

        Employee e = new Employee();
        e.setId(id);
        e.setWorkEmailAddress("john@company.com");
        e.setFullName("John Doe");
        e.setProfileCompletion(100);
        e.setDepartmentId(UUID.randomUUID().toString());
        return e;
    }


    @Test
    void createLeaveRequest_success() {

        CreateLeaveRequest req = new CreateLeaveRequest();

        req.setNumberOfLeaveDays(10);
        req.setLeaveType(any());

        var emp = mockEmployee(UUID.randomUUID().toString());

        when(employeeRepo.findByWorkEmailAddressIgnoreCase("john@company.com")).thenReturn(Mono.just(emp));
        when(leaveRepo.existsByEmployeeEmailAddressAndIsActive("john@company.com", true)).thenReturn(Mono.just(false));
        when(leaveRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var auth = new TestingAuthenticationToken("john@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<String>> result = service.submitLeaveRequest(Mono.just(req))
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

}