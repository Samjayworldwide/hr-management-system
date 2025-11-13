package com.samjay.hr_management_system.services;

import com.samjay.hr_management_system.dtos.request.LoginRequest;
import com.samjay.hr_management_system.dtos.request.PasswordChangeRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.LoginResponse;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    Mono<ApiResponse<LoginResponse>> login(Mono<LoginRequest> loginRequestMono);

    Mono<ApiResponse<String>> changeLoginPassword(Mono<PasswordChangeRequest> passwordChangeRequestMono);

}
