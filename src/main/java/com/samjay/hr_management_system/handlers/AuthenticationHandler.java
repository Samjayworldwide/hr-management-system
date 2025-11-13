package com.samjay.hr_management_system.handlers;

import com.samjay.hr_management_system.dtos.request.LoginRequest;
import com.samjay.hr_management_system.dtos.request.PasswordChangeRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.LoginResponse;
import com.samjay.hr_management_system.services.AuthenticationService;
import com.samjay.hr_management_system.utils.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationHandler {

    private final AuthenticationService authenticationService;

    private final RequestValidator requestValidator;

    public Mono<ServerResponse> loginEmployeeHandler(ServerRequest serverRequest) {

        Mono<LoginRequest> loginRequestMono = serverRequest.bodyToMono(LoginRequest.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<LoginResponse>> apiResponseMono = authenticationService.login(loginRequestMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> changeLoginPasswordHandler(ServerRequest serverRequest) {

        Mono<PasswordChangeRequest> passwordChangeRequestMono = serverRequest.bodyToMono(PasswordChangeRequest.class);

        Mono<ApiResponse<String>> apiResponseMono = authenticationService.changeLoginPassword(passwordChangeRequestMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }
}
