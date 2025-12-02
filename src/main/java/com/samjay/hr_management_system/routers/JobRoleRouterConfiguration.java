package com.samjay.hr_management_system.routers;

import com.samjay.hr_management_system.dtos.request.CreateJobRoleRequest;
import com.samjay.hr_management_system.dtos.request.UpdateJobRoleRequest;
import com.samjay.hr_management_system.handlers.JobRoleHandler;
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
public class JobRoleRouterConfiguration {

    private final JobRoleHandler jobRoleHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/create-a-job-role",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = JobRoleHandler.class,
                            beanMethod = "createJobRoleHandler",
                            operation = @Operation(
                                    operationId = "createJobRoleHandler",
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
                                                            implementation = CreateJobRoleRequest.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> createJobRoleRouterFunction() {

        return RouterFunctions
                .route()
                .POST("/create-a-job-role", jobRoleHandler::createJobRoleHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/get-all-job-roles",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = JobRoleHandler.class,
                            beanMethod = "fetchAllJobRolesHandler",
                            operation = @Operation(
                                    operationId = "fetchAllJobRolesHandler",
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
    public RouterFunction<ServerResponse> fetchAllJobRolesRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/get-all-job-roles", jobRoleHandler::fetchAllJobRolesHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/get-job-role-by-id/{jobRoleId}",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = JobRoleHandler.class,
                            beanMethod = "fetchAJobRoleByIdHandler",
                            operation = @Operation(
                                    operationId = "fetchAJobRoleByIdHandler",
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
                                    parameters = @Parameter(in = ParameterIn.PATH, name = "jobRoleId")

                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> fetchJobRoleByIdRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/get-job-role-by-id/{jobRoleId}", jobRoleHandler::fetchAJobRoleByIdHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/update-a-job-role/{jobRoleId}",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.PUT,
                            beanClass = JobRoleHandler.class,
                            beanMethod = "updateJobRoleHandler",
                            operation = @Operation(
                                    operationId = "updateJobRoleHandler",
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
                                    parameters = @Parameter(in = ParameterIn.PATH, name = "jobRoleId"),
                                    requestBody = @RequestBody(
                                            content = @Content(
                                                    schema = @Schema(
                                                            implementation = UpdateJobRoleRequest.class
                                                    )
                                            )
                                    )

                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> updateJobRoleHandler() {

        return RouterFunctions
                .route()
                .PUT("/update-a-job-role/{jobRoleId}", jobRoleHandler::updateJobRoleHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/delete-a-job-role/{jobRoleId}",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.DELETE,
                            beanClass = JobRoleHandler.class,
                            beanMethod = "deleteJobRoleHandler",
                            operation = @Operation(
                                    operationId = "deleteJobRoleHandler",
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
                                    parameters = @Parameter(in = ParameterIn.PATH, name = "jobRoleId")

                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> deleteJobHandler() {

        return RouterFunctions
                .route()
                .DELETE("/delete-a-job-role/{jobRoleId}", jobRoleHandler::deleteJobRoleHandler)
                .build();
    }
}
