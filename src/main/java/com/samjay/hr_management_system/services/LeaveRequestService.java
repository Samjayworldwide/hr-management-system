package com.samjay.hr_management_system.services;

import com.samjay.hr_management_system.dtos.request.CreateLeaveRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.ActiveLeaveResponse;
import com.samjay.hr_management_system.dtos.response.LeaveResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LeaveRequestService {

    Mono<ApiResponse<String>> submitLeaveRequest(Mono<CreateLeaveRequest> createLeaveRequestMono);

    Mono<ApiResponse<String>> approveLeaveRequest(String id);

    Mono<ApiResponse<List<LeaveResponse>>> getAllUnapprovedLeaveRequests();

    Mono<ApiResponse<List<ActiveLeaveResponse>>> getAllEmployeesCurrentlyOnLeave();
}
