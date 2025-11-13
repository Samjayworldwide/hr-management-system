package com.samjay.hr_management_system.handlers;

import com.samjay.hr_management_system.dtos.request.CreateDepartmentRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.services.DepartmentService;
import com.samjay.hr_management_system.utils.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DepartmentHandler {

    private final DepartmentService departmentService;

    private final RequestValidator requestValidator;

    public Mono<ServerResponse> createDepartmentHandler(ServerRequest serverRequest) {

        Mono<CreateDepartmentRequest> createDepartmentRequestMono = serverRequest.bodyToMono(CreateDepartmentRequest.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> departmentApiResponse = departmentService.createDepartment(createDepartmentRequestMono);

        return departmentApiResponse.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }
}
