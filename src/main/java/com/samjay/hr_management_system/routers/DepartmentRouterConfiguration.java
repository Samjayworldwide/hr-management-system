package com.samjay.hr_management_system.routers;

import com.samjay.hr_management_system.dtos.request.CreateDepartmentRequest;
import com.samjay.hr_management_system.dtos.request.UpdateDepartmentRequest;
import com.samjay.hr_management_system.handlers.DepartmentHandler;
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
public class DepartmentRouterConfiguration {

    private final DepartmentHandler departmentHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/create-a-department",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = DepartmentHandler.class,
                            beanMethod = "createDepartmentHandler",
                            operation = @Operation(
                                    operationId = "createDepartmentHandler",
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
                                                            implementation = CreateDepartmentRequest.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> createDepartmentRouterFunction() {

        return RouterFunctions
                .route()
                .POST("/create-a-department", departmentHandler::createDepartmentHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/get-all-departments",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = DepartmentHandler.class,
                            beanMethod = "getAllDepartmentsHandler",
                            operation = @Operation(
                                    operationId = "getAllDepartmentsHandler",
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
    public RouterFunction<ServerResponse> getAllDepartmentsRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/get-all-departments", departmentHandler::getAllDepartmentsHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/get-a-department-and-its-job-roles/{departmentId}",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = DepartmentHandler.class,
                            beanMethod = "getDepartmentAndJobRolesHandler",
                            operation = @Operation(
                                    operationId = "getDepartmentAndJobRolesHandler",
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
                                    parameters = @Parameter(in = ParameterIn.PATH, name = "departmentId")
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> getADepartmentAndItsJobRolesRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/get-a-department-and-its-job-roles/{departmentId}", departmentHandler::getDepartmentAndJobRolesHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/update-department/{departmentId}",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.PUT,
                            beanClass = DepartmentHandler.class,
                            beanMethod = "updateDepartmentHandler",
                            operation = @Operation(
                                    operationId = "updateDepartmentHandler",
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
                                    parameters = @Parameter(in = ParameterIn.PATH, name = "departmentId"),
                                    requestBody = @RequestBody(
                                            content = @Content(
                                                    schema = @Schema(
                                                            implementation = UpdateDepartmentRequest.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> updateDepartmentRouterFunction() {

        return RouterFunctions
                .route()
                .PUT("/update-department/{departmentId}", departmentHandler::updateDepartmentHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/delete-department",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.DELETE,
                            beanClass = DepartmentHandler.class,
                            beanMethod = "deleteDepartmentHandler",
                            operation = @Operation(
                                    operationId = "deleteDepartmentHandler",
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
                                    parameters = @Parameter(in = ParameterIn.QUERY, name = "departmentId")
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> deleteDepartmentRouterFunction() {

        return RouterFunctions
                .route()
                .DELETE("/delete-department", departmentHandler::deleteDepartmentHandler)
                .build();
    }
}
