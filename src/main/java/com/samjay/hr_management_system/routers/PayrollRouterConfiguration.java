package com.samjay.hr_management_system.routers;

import com.samjay.hr_management_system.handlers.LeaveRequestHandler;
import com.samjay.hr_management_system.handlers.PayrollHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class PayrollRouterConfiguration {

    private final PayrollHandler payrollHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/payroll-records/{payrollPeriod}",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = PayrollHandler.class,
                            beanMethod = "getAllEmployeePayrollRecordsForAMonthHandler",
                            operation = @Operation(
                                    operationId = "getAllEmployeePayrollRecordsForAMonthHandler",
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
                                    parameters = @Parameter(in = ParameterIn.PATH, name = "payrollPeriod")
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> getAllEmployeesPayrollRecordsForAMonthRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/payroll-records/{payrollPeriod}", payrollHandler::getAllEmployeePayrollRecordsForAMonthHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/employee-/payroll-record/{payrollPeriod}",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = PayrollHandler.class,
                            beanMethod = "getAnEmployeePayrollRecordsForAMonthHandler",
                            operation = @Operation(
                                    operationId = "getAnEmployeePayrollRecordsForAMonthHandler",
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
                                    parameters = @Parameter(in = ParameterIn.PATH, name = "payrollPeriod")
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> getAnEmployeePayrollRecordsForAMonthRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/employee-/payroll-record/{payrollPeriod}", payrollHandler::getAnEmployeePayrollRecordsForAMonthHandler)
                .build();
    }
}
