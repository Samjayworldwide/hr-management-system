package com.samjay.hr_management_system.services.implementations;

import com.samjay.hr_management_system.dtos.request.LoginRequest;
import com.samjay.hr_management_system.dtos.request.PasswordChangeRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.LoginResponse;
import com.samjay.hr_management_system.repositories.EmployeeRepository;
import com.samjay.hr_management_system.security.JWTUtil;
import com.samjay.hr_management_system.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImplementation implements AuthenticationService {

    private final JWTUtil jwtUtil;

    private final EmployeeRepository employeeRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<ApiResponse<LoginResponse>> login(Mono<LoginRequest> loginRequestMono) {

        return loginRequestMono.flatMap(loginRequest ->

                employeeRepository.findByWorkEmailAddressIgnoreCase(loginRequest.getWorkEmailAddress().trim())
                        .flatMap(employee -> {

                            if (!passwordEncoder.matches(loginRequest.getPassword(), employee.getPassword()))
                                return Mono.just(ApiResponse.<LoginResponse>error("Invalid email or password"));

                            return Mono.fromCallable(() -> jwtUtil.generateToken(employee.getWorkEmailAddress(), List.of(employee.getRole().name())))
                                    .map(jwtToken -> {

                                        LoginResponse loginResponse = new LoginResponse();

                                        loginResponse.setFirstname(employee.getFirstname());

                                        loginResponse.setLastname(employee.getLastname());

                                        loginResponse.setJwtToken(jwtToken);

                                        return ApiResponse.success("Login successful", loginResponse);

                                    });

                        })
                        .switchIfEmpty(Mono.just(ApiResponse.error("Invalid email or password")))
                        .onErrorResume(error -> {

                            log.error("An unexpected error occurred logging in " + error.getMessage());

                            return Mono.just(ApiResponse.error("Login failed"));

                        })
        );
    }

    @Override
    public Mono<ApiResponse<String>> changeLoginPassword(Mono<PasswordChangeRequest> passwordChangeRequestMono) {

        return passwordChangeRequestMono
                .flatMap(passwordChangeRequest -> ReactiveSecurityContextHolder
                        .getContext()
                        .flatMap(securityContext -> {

                            String email = securityContext.getAuthentication().getName();

                            return employeeRepository.findByWorkEmailAddressIgnoreCase(email)
                                    .flatMap(employee -> {

                                        if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword().trim(), employee.getPassword().trim()))
                                            return Mono.just(ApiResponse.<String>error("Invalid password"));

                                        if (!passwordChangeRequest.getNewPassword().trim().equalsIgnoreCase(passwordChangeRequest.getConfirmNewPassword().trim()))
                                            return Mono.just(ApiResponse.<String>error("New password and confirm new password does not match"));

                                        employee.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));

                                        return employeeRepository
                                                .save(employee)
                                                .thenReturn(ApiResponse.<String>success("Password changed successfully"));
                                    })
                                    .switchIfEmpty(Mono.just(ApiResponse.error("Could not find employee with given email")));
                        })
                ).onErrorResume(error -> {

                    log.error("An unexpected error occurred changing login password {} ", error.getMessage());

                    return Mono.just(ApiResponse.error("Failed to change login password"));

                });
    }

}
