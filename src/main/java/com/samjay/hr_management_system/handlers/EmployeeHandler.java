package com.samjay.hr_management_system.handlers;

import com.samjay.hr_management_system.dtos.request.CompleteProfileRequest;
import com.samjay.hr_management_system.dtos.request.CreateEmployeeRequest;
import com.samjay.hr_management_system.dtos.request.CreateHrRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.EmployeeProfileResponse;
import com.samjay.hr_management_system.dtos.response.EmployeeResponse;
import com.samjay.hr_management_system.globalexception.RequestValidationException;
import com.samjay.hr_management_system.services.EmployeeService;
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
public class EmployeeHandler {

    private final EmployeeService employeeService;

    private final RequestValidator requestValidator;

    public Mono<ServerResponse> createEmployeeHandler(ServerRequest serverRequest) {

        Mono<CreateEmployeeRequest> createEmployeeRequestMono = serverRequest.bodyToMono(CreateEmployeeRequest.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> apiResponseMono = employeeService.createNewEmployee(createEmployeeRequestMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> createHrHandler(ServerRequest serverRequest) {

        Mono<CreateHrRequest> createHrRequestMono = serverRequest.bodyToMono(CreateHrRequest.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> apiResponseMono = employeeService.createHR(createHrRequestMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> completeEmployeeInformationHandler(ServerRequest serverRequest) {

        Mono<CompleteProfileRequest> completeProfileRequestMono = serverRequest.bodyToMono(CompleteProfileRequest.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> apiResponseMono = employeeService.completeEmployeeInformation(completeProfileRequestMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    @SuppressWarnings("unused")
    public Mono<ServerResponse> getProfileCompletionProgressHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<String>> apiResponseMono = employeeService.getProfileCompletionProgress();

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> searchEmployeeByWorkEmailAddressHandler(ServerRequest serverRequest) {

        String workEmailAddress = serverRequest.pathVariable("workEmailAddress");

        if (workEmailAddress.isEmpty() || workEmailAddress == null)
            throw new RequestValidationException("Work email address is required");

        Mono<ApiResponse<EmployeeResponse>> apiResponseMono = employeeService.searchEmployeeByWorkEmailAddress(workEmailAddress);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    @SuppressWarnings("unused")
    public Mono<ServerResponse> getEmployeeProfileHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<EmployeeProfileResponse>> apiResponseMono = employeeService.getEmployeeProfile();

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    @SuppressWarnings("unused")
    public Mono<ServerResponse> fetchAllEmployeesHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<List<EmployeeResponse>>> apiResponseMono = employeeService.fetchAllEmployees();

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> terminateEmployeeHandler(ServerRequest serverRequest) {

        Optional<String> idOpt = serverRequest.queryParam("employeeId");

        if (idOpt.isEmpty())
            return ServerResponse.badRequest().bodyValue(ApiResponse.error("Employee Id is required"));

        Mono<ApiResponse<String>> apiResponseMono = employeeService.terminateEmployee(idOpt.get());

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }
}
