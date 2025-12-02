package com.samjay.hr_management_system.configurations;

import com.samjay.hr_management_system.security.JWTAuthenticationManager;
import com.samjay.hr_management_system.security.JWTSecurityContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JWTAuthenticationManager JWTAuthenticationManager;

    private final JWTSecurityContextRepository jwtSecurityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {

        return serverHttpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authenticationManager(JWTAuthenticationManager)
                .securityContextRepository(jwtSecurityContextRepository)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/login",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-doc/**",
                                "/webjars/**")
                        .permitAll()
                        .pathMatchers("/create-an-employee",
                                "/approve-leave-request",
                                "/search-employee-by-work-email/{workEmailAddress}",
                                "/fetch-all-employees",
                                "/terminate-employee",
                                "/get-a-department-and-its-job-roles/{departmentId}",
                                "/payroll-records/{payrollPeriod}",
                                "/get-all-leave-requests",
                                "/get-all-employees-currently-on-leave").hasAnyAuthority("ADMIN_ROLE", "HR_ROLE")
                        .pathMatchers("/complete-profile-information",
                                "/get-profile-completion-progress",
                                "/change-login-password",
                                "/submit-leave-request",
                                "/employee-/payroll-record/{payrollPeriod}").hasAnyAuthority("HR_ROLE", "EMPLOYEE_ROLE")
                        .pathMatchers("/create-a-job-role",
                                "/create-an-hr",
                                "/create-a-department",
                                "/get-all-job-roles",
                                "/get-job-role-by-id/{jobRoleId}",
                                "/update-a-job-role/{jobRoleId}",
                                "/delete-a-job-role/{jobRoleId}",
                                "/update-department/{departmentId}",
                                "/delete-department").hasAuthority("ADMIN_ROLE")
                        .anyExchange().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, authEx) -> {

                            exchange.getResponse().getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);

                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

                            return exchange.getResponse().setComplete();

                        })
                        .accessDeniedHandler((exchange, denied) -> {

                            exchange.getResponse().getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);

                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);

                            return exchange.getResponse().setComplete();

                        })
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }
}
