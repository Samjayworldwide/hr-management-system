package com.samjay.hr_management_system.services;

import com.samjay.hr_management_system.dtos.request.CreateDepartmentRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import reactor.core.publisher.Mono;

public interface DepartmentService {

    Mono<ApiResponse<String>> createDepartment(Mono<CreateDepartmentRequest> createDepartmentRequestMono);

}
