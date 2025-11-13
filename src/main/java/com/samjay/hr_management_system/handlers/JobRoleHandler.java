package com.samjay.hr_management_system.handlers;

import com.samjay.hr_management_system.dtos.request.CreateJobRoleRequest;
import com.samjay.hr_management_system.dtos.request.UpdateJobRoleRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.JobRoleResponse;
import com.samjay.hr_management_system.globalexception.RequestValidationException;
import com.samjay.hr_management_system.services.JobRoleService;
import com.samjay.hr_management_system.utils.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JobRoleHandler {

    private final JobRoleService jobRoleService;

    private final RequestValidator requestValidator;

    public Mono<ServerResponse> createJobRoleHandler(ServerRequest serverRequest) {

        Mono<CreateJobRoleRequest> createJobRoleRequestMono = serverRequest.bodyToMono(CreateJobRoleRequest.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> jobRoleApiResponse = jobRoleService.createJobRole(createJobRoleRequestMono);

        return jobRoleApiResponse.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    @SuppressWarnings("unused")
    public Mono<ServerResponse> fetchAllJobRolesHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<List<JobRoleResponse>>> jobRoleApiResponse = jobRoleService.fetchAllJobRoles();

        return jobRoleApiResponse.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> fetchAJobRoleByIdHandler(ServerRequest serverRequest) {

        String id = serverRequest.pathVariable("jobRoleId");

        if (id.isEmpty() || id == null)
            throw new RequestValidationException("Job role Id is required");

        Mono<ApiResponse<JobRoleResponse>> jobRoleApiResponse = jobRoleService.fetchAJobRole(id);

        return jobRoleApiResponse.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> updateJobRoleHandler(ServerRequest serverRequest) {

        String id = serverRequest.pathVariable("jobRoleId");

        if (id.isEmpty() || id == null)
            throw new RequestValidationException("Job role Id is required");

        Mono<UpdateJobRoleRequest> updateJobRoleRequestMono = serverRequest.bodyToMono(UpdateJobRoleRequest.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> jobRoleApiResponse = jobRoleService.updateAJobRole(id, updateJobRoleRequestMono);

        return jobRoleApiResponse.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> deleteJobRoleHandler(ServerRequest serverRequest) {

        String id = serverRequest.pathVariable("jobRoleId");

        if (id.isEmpty() || id == null)
            throw new RequestValidationException("Job role Id is required");

        Mono<ApiResponse<String>> jobRoleApiResponse = jobRoleService.deleteJobRole(id);

        return jobRoleApiResponse.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }
}
