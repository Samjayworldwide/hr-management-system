package com.samjay.hr_management_system.services;

import com.samjay.hr_management_system.dtos.request.CompleteProfileRequest;
import com.samjay.hr_management_system.dtos.request.CreateEmployeeRequest;
import com.samjay.hr_management_system.dtos.request.CreateHrRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.EmployeeProfileResponse;
import com.samjay.hr_management_system.dtos.response.EmployeeResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EmployeeService {

    Mono<ApiResponse<String>> createNewEmployee(Mono<CreateEmployeeRequest> createEmployeeRequestMono);

    Mono<ApiResponse<String>> createHR(Mono<CreateHrRequest> createHrRequestMono);

    Mono<ApiResponse<String>> completeEmployeeInformation(Mono<CompleteProfileRequest> completeProfileRequestMono);

    Mono<ApiResponse<String>> getProfileCompletionProgress();

    Mono<ApiResponse<EmployeeResponse>> searchEmployeeByWorkEmailAddress(String workEmailAddress);

    Mono<ApiResponse<EmployeeProfileResponse>> getEmployeeProfile();

    Mono<ApiResponse<List<EmployeeResponse>>> fetchAllEmployees();

    Mono<ApiResponse<String>> terminateEmployee(String id);

}
