package com.samjay.hr_management_system.routers;

import com.samjay.hr_management_system.dtos.request.CreateLeaveRequest;
import com.samjay.hr_management_system.handlers.LeaveRequestHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class LeaveRequestRouterConfiguration {

    private final LeaveRequestHandler leaveRequestHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/submit-leave-request",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = LeaveRequestHandler.class,
                            beanMethod = "submitLeaveRequestHandler",
                            operation = @Operation(
                                    operationId = "submitLeaveRequestHandler",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful operation",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.hr_management_system.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            ),
                                            @ApiResponse(
                                                    responseCode = "400",
                                                    description = "Bad request",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.hr_management_system.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            content = @Content(
                                                    schema = @Schema(
                                                            implementation = CreateLeaveRequest.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> submitLeaveRequestRouterFunction() {

        return RouterFunctions
                .route()
                .POST("/submit-leave-request", leaveRequestHandler::submitLeaveRequestHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/approve-leave-request",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = LeaveRequestHandler.class,
                            beanMethod = "approveLeaveRequestHandler",
                            operation = @Operation(
                                    operationId = "approveLeaveRequestHandler",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful operation",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.hr_management_system.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            ),
                                            @ApiResponse(
                                                    responseCode = "400",
                                                    description = "Bad request",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.hr_management_system.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            )
                                    },
                                    parameters = @Parameter(in = ParameterIn.QUERY, name = "leaveRequestId")
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> approveLeaveRequest() {

        return RouterFunctions
                .route()
                .POST("/approve-leave-request", leaveRequestHandler::approveLeaveRequestHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/get-all-leave-requests",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = LeaveRequestHandler.class,
                            beanMethod = "getAllLeaveRequestsHandler",
                            operation = @Operation(
                                    operationId = "getAllLeaveRequestsHandler",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful operation",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.hr_management_system.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            ),
                                            @ApiResponse(
                                                    responseCode = "400",
                                                    description = "Bad request",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.hr_management_system.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            )
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> getAllLeaveRequestsRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/get-all-leave-requests", leaveRequestHandler::getAllLeaveRequestsHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/get-all-employees-currently-on-leave",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = LeaveRequestHandler.class,
                            beanMethod = "getAllEmployeesCurrentlyOnLeaveHandler",
                            operation = @Operation(
                                    operationId = "getAllEmployeesCurrentlyOnLeaveHandler",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful operation",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.hr_management_system.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            ),
                                            @ApiResponse(
                                                    responseCode = "400",
                                                    description = "Bad request",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.hr_management_system.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            )
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> getAllEmployeesCurrentlyOnLeaveRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/get-all-employees-currently-on-leave", leaveRequestHandler::getAllEmployeesCurrentlyOnLeaveHandler)
                .build();
    }
}
