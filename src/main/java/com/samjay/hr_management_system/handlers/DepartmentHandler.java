package com.samjay.hr_management_system.handlers;

import com.samjay.hr_management_system.dtos.request.CreateDepartmentRequest;
import com.samjay.hr_management_system.dtos.request.UpdateDepartmentRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.DepartmentAndJobRoleResponse;
import com.samjay.hr_management_system.dtos.response.DepartmentResponse;
import com.samjay.hr_management_system.globalexception.RequestValidationException;
import com.samjay.hr_management_system.services.DepartmentService;
import com.samjay.hr_management_system.utils.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DepartmentHandler {

    private final DepartmentService departmentService;

    private final RequestValidator requestValidator;

    public Mono<ServerResponse> createDepartmentHandler(ServerRequest serverRequest) {

        Mono<CreateDepartmentRequest> createDepartmentRequestMono = serverRequest.bodyToMono(CreateDepartmentRequest.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> apiResponseMono = departmentService.createDepartment(createDepartmentRequestMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    @SuppressWarnings("unused")
    public Mono<ServerResponse> getAllDepartmentsHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<List<DepartmentResponse>>> apiResponseMono = departmentService.getAllDepartments();

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> getDepartmentAndJobRolesHandler(ServerRequest serverRequest) {

        String departmentId = serverRequest.pathVariable("departmentId");

        Mono<ApiResponse<DepartmentAndJobRoleResponse>> apiResponseMono = departmentService.getADepartmentAndItsJobRoles(departmentId);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> updateDepartmentHandler(ServerRequest serverRequest) {

        String departmentId = serverRequest.pathVariable("departmentId");

        Mono<UpdateDepartmentRequest> updateDepartmentRequestMono = serverRequest.bodyToMono(UpdateDepartmentRequest.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> apiResponseMono = departmentService.updateDepartment(departmentId, updateDepartmentRequestMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> deleteDepartmentHandler(ServerRequest serverRequest) {

        Optional<String> departmentIdOptional = serverRequest.queryParam("departmentId");

        if (departmentIdOptional.isEmpty())
            throw new RequestValidationException("Department ID is required");

        Mono<ApiResponse<String>> apiResponseMono = departmentService.deleteDepartment(departmentIdOptional.get());

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }
}
