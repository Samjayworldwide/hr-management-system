package com.samjay.hr_management_system.handlers;

import com.samjay.hr_management_system.dtos.request.CreateLeaveRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.ActiveLeaveResponse;
import com.samjay.hr_management_system.dtos.response.LeaveResponse;
import com.samjay.hr_management_system.services.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LeaveRequestHandler {

    private final LeaveRequestService leaveRequestService;

    public Mono<ServerResponse> submitLeaveRequestHandler(ServerRequest serverRequest) {

        Mono<CreateLeaveRequest> createLeaveRequestMono = serverRequest.bodyToMono(CreateLeaveRequest.class);

        Mono<ApiResponse<String>> apiResponseMono = leaveRequestService.submitLeaveRequest(createLeaveRequestMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    public Mono<ServerResponse> approveLeaveRequestHandler(ServerRequest serverRequest) {

        Optional<String> idOpt = serverRequest.queryParam("leaveRequestId");

        if (idOpt.isEmpty())
            return ServerResponse.badRequest().bodyValue(ApiResponse.error("Leave Request Id is required"));

        Mono<ApiResponse<String>> apiResponseMono = leaveRequestService.approveLeaveRequest(idOpt.get());

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    @SuppressWarnings("unused")
    public Mono<ServerResponse> getAllLeaveRequestsHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<List<LeaveResponse>>> apiResponseMono = leaveRequestService.getAllUnapprovedLeaveRequests();

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }

    @SuppressWarnings("unused")
    public Mono<ServerResponse> getAllEmployeesCurrentlyOnLeaveHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<List<ActiveLeaveResponse>>> apiResponseMono = leaveRequestService.getAllEmployeesCurrentlyOnLeave();

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });

    }
}
