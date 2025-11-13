package com.samjay.hr_management_system.services;

import com.samjay.hr_management_system.dtos.request.CreateJobRoleRequest;
import com.samjay.hr_management_system.dtos.request.UpdateJobRoleRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.JobRoleResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface JobRoleService {

    Mono<ApiResponse<String>> createJobRole(Mono<CreateJobRoleRequest> createJobRoleRequestMono);

    Mono<ApiResponse<List<JobRoleResponse>>> fetchAllJobRoles();

    Mono<ApiResponse<JobRoleResponse>> fetchAJobRole(String id);

    Mono<ApiResponse<String>> updateAJobRole(String id, Mono<UpdateJobRoleRequest> updateJobRoleRequestMono);

    Mono<ApiResponse<String>> deleteJobRole(String id);
}