package com.samjay.hr_management_system.services;

import com.samjay.hr_management_system.dtos.request.CreateDepartmentRequest;
import com.samjay.hr_management_system.dtos.request.UpdateDepartmentRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.DepartmentAndJobRoleResponse;
import com.samjay.hr_management_system.dtos.response.DepartmentResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DepartmentService {

    Mono<ApiResponse<String>> createDepartment(Mono<CreateDepartmentRequest> createDepartmentRequestMono);

    Mono<ApiResponse<List<DepartmentResponse>>> getAllDepartments();

    Mono<ApiResponse<DepartmentAndJobRoleResponse>> getADepartmentAndItsJobRoles(String departmentId);

    Mono<ApiResponse<String>> updateDepartment(String departmentId, Mono<UpdateDepartmentRequest> updateDepartmentRequestMono);

    Mono<ApiResponse<String>> deleteDepartment(String departmentId);

}
