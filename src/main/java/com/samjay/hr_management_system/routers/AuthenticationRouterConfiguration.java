package com.samjay.hr_management_system.routers;

import com.samjay.hr_management_system.dtos.request.LoginRequest;
import com.samjay.hr_management_system.dtos.request.PasswordChangeRequest;
import com.samjay.hr_management_system.handlers.AuthenticationHandler;
import io.swagger.v3.oas.annotations.Operation;
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
public class AuthenticationRouterConfiguration {

    private final AuthenticationHandler authenticationHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/login",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = AuthenticationHandler.class,
                            beanMethod = "loginEmployeeHandler",
                            operation = @Operation(
                                    operationId = "loginEmployeeHandler",
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
                                                            implementation = LoginRequest.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> loginRouterFunction() {

        return RouterFunctions
                .route()
                .POST("/login", authenticationHandler::loginEmployeeHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/change-login-password",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = AuthenticationHandler.class,
                            beanMethod = "changeLoginPasswordHandler",
                            operation = @Operation(
                                    operationId = "changeLoginPasswordHandler",
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
                                                            implementation = PasswordChangeRequest.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> changeLoginPasswordRouterFunction() {

        return RouterFunctions
                .route()
                .POST("/change-login-password", authenticationHandler::changeLoginPasswordHandler)
                .build();
    }
}
