package com.samjay.hr_management_system.services.implementations;

import com.samjay.hr_management_system.constants.Constant;
import com.samjay.hr_management_system.dtos.request.*;
import com.samjay.hr_management_system.dtos.response.EmployeeProfileResponse;
import com.samjay.hr_management_system.dtos.response.EmployeeResponse;
import com.samjay.hr_management_system.enumerations.EmploymentStatus;
import com.samjay.hr_management_system.globalexception.ApplicationException;
import com.samjay.hr_management_system.publishers.EmailPublisher;
import com.samjay.hr_management_system.utils.Utility;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.entities.Employee;
import com.samjay.hr_management_system.repositories.DepartmentRepository;
import com.samjay.hr_management_system.repositories.EmployeeRepository;
import com.samjay.hr_management_system.repositories.JobRoleRepository;
import com.samjay.hr_management_system.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import static com.samjay.hr_management_system.constants.Constant.*;
import static com.samjay.hr_management_system.utils.Utility.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImplementation implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final JobRoleRepository jobRoleRepository;

    private final DepartmentRepository departmentRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailPublisher emailPublisher;

    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;

    @Override
    public Mono<ApiResponse<String>> createNewEmployee(Mono<CreateEmployeeRequest> createEmployeeRequestMono) {

        return createEmployeeRequestMono.flatMap(createEmployeeRequest ->

                employeeRepository.existsByPersonalEmailAddressIgnoreCase(createEmployeeRequest.getPersonalEmailAddress())
                        .flatMap(personalEmailExists -> {

                            if (personalEmailExists)
                                return Mono.just(ApiResponse.<String>error("An employee already exists with the given email"));

                            String workEmailAddress = createEmployeeRequest.getWorkEmailPrefix() + Constant.WORK_EMAIL_SUFFIX;

                            return employeeRepository.existsByWorkEmailAddressIgnoreCase(workEmailAddress)
                                    .flatMap(workEmailAddressExists -> {

                                        if (workEmailAddressExists)
                                            return Mono.just(ApiResponse.<String>error("A work email already exists with the prefix given"));

                                        return jobRoleRepository.findByJobPositionIgnoreCase(createEmployeeRequest.getJobPosition())
                                                .flatMap(jobPosition ->
                                                        departmentRepository.findById(jobPosition.getDepartmentId())
                                                                .flatMap(department -> ReactiveSecurityContextHolder
                                                                        .getContext()
                                                                        .flatMap(securityContext -> {

                                                                            List<String> roles = securityContext
                                                                                    .getAuthentication()
                                                                                    .getAuthorities()
                                                                                    .stream()
                                                                                    .map(GrantedAuthority::getAuthority)
                                                                                    .toList();

                                                                            boolean isAdmin = roles.contains("ADMIN_ROLE");

                                                                            boolean isjobPositionInHR = department.getDepartmentName().equalsIgnoreCase(HR_DEPARTMENT);

                                                                            if (isAdmin && isjobPositionInHR)
                                                                                return Mono.just(ApiResponse.<String>error("Please create this employee using the create HR endpoint"));

                                                                            if (!isAdmin && isjobPositionInHR)
                                                                                return Mono.just(ApiResponse.<String>error("Only admins can create employees in the HR department"));

                                                                            String defaultPassword = generateDefaultPassword();

                                                                            String email = securityContext.getAuthentication().getName();

                                                                            String hashedPassword = passwordEncoder.encode(defaultPassword);

                                                                            Employee employee = mapToEmployeeFromCreateEmployeeRequest(createEmployeeRequest, workEmailAddress, hashedPassword, email, department);

                                                                            Long currentNumberOfEmployees = department.getNumberOfEmployees();

                                                                            department.setNumberOfEmployees(currentNumberOfEmployees + 1);

                                                                            return departmentRepository.save(department)
                                                                                    .then(employeeRepository.save(employee))
                                                                                    .doOnSuccess(ignored -> {

                                                                                        EmailDetails emailDetails = EmailDetails
                                                                                                .builder()
                                                                                                .subject("ONBOARDING COMPLETION")
                                                                                                .recipient(createEmployeeRequest.getPersonalEmailAddress())
                                                                                                .messageBody(Utility.getOnboardingSuccessEmail(workEmailAddress, createEmployeeRequest.getFirstname()))
                                                                                                .build();

                                                                                        emailPublisher.queueEmail(emailDetails)
                                                                                                .doOnError(error -> log.warn("Failed to queue onboarding email: {}", error.getMessage()))
                                                                                                .onErrorComplete()
                                                                                                .subscribeOn(Schedulers.boundedElastic())
                                                                                                .subscribe();
                                                                                    })
                                                                                    .then(reactiveRedisOperations.delete(CACHE_KEY_ALL_EMPLOYEES))
                                                                                    .doOnError(error -> log.error("An unexpected error occurred deleting from redis {}", error.getMessage()))
                                                                                    .thenReturn(ApiResponse.<String>success("Employee saved successfully."))
                                                                                    .onErrorReturn(ApiResponse.success("Employee saved successfully."));
                                                                        }))
                                                                .switchIfEmpty(Mono.just(ApiResponse.error("Department does not exist")))

                                                ).switchIfEmpty(Mono.just(ApiResponse.error("Invalid job position")));
                                    });
                        }).onErrorResume(error -> Mono.just(ApiResponse.error("Failed to create employee: " + error.getMessage())))
        );
    }

    @Override
    public Mono<ApiResponse<String>> createHR(Mono<CreateHrRequest> createHrRequestMono) {

        return createHrRequestMono.flatMap(createHrRequest ->
                        employeeRepository.existsByPersonalEmailAddressIgnoreCase(createHrRequest.getPersonalEmailAddress())
                                .flatMap(personalEmailExists -> {

                                    if (personalEmailExists)
                                        return Mono.just(ApiResponse.<String>error("An employee already exists with the given personal email"));

                                    String workEmailAddress = createHrRequest.getWorkEmailPrefix() + Constant.WORK_EMAIL_SUFFIX;

                                    return employeeRepository.existsByWorkEmailAddressIgnoreCase(workEmailAddress)
                                            .flatMap(workEmailAddressExists -> {

                                                if (workEmailAddressExists)
                                                    return Mono.just(ApiResponse.<String>error("An employee already exists with given work email prefix"));

                                                return jobRoleRepository.findByJobPositionIgnoreCase(createHrRequest.getJobPosition())
                                                        .flatMap(jobRole -> departmentRepository.findById(jobRole.getDepartmentId())
                                                                .flatMap(department -> ReactiveSecurityContextHolder
                                                                        .getContext()
                                                                        .flatMap(securityContext -> {

                                                                            String email = securityContext.getAuthentication().getName();

                                                                            if (!department.getDepartmentName().equalsIgnoreCase(HR_DEPARTMENT))
                                                                                return Mono.just(ApiResponse.<String>error("You can only add employees in the HR department"));

                                                                            String defaultPassword = generateDefaultPassword();

                                                                            String hashedPassword = passwordEncoder.encode(defaultPassword);

                                                                            Employee employee = mapToEmployeeFromCreateHrRequest(createHrRequest, hashedPassword, workEmailAddress, email, department);

                                                                            Long currentNumberOfEmployees = department.getNumberOfEmployees();

                                                                            department.setNumberOfEmployees(currentNumberOfEmployees + 1);

                                                                            return departmentRepository.save(department)
                                                                                    .then(employeeRepository.save(employee))
                                                                                    .doOnSuccess(ignored -> {

                                                                                        EmailDetails emailDetails = EmailDetails
                                                                                                .builder()
                                                                                                .messageBody(Utility.getOnboardingSuccessEmail(workEmailAddress, defaultPassword))
                                                                                                .recipient(createHrRequest.getPersonalEmailAddress())
                                                                                                .subject("ONBOARDING COMPLETION")
                                                                                                .build();

                                                                                        emailPublisher.queueEmail(emailDetails)
                                                                                                .doOnError(error -> log.warn("Failed to queue onboarding email: {}", error.getMessage()))
                                                                                                .onErrorComplete()
                                                                                                .subscribeOn(Schedulers.boundedElastic())
                                                                                                .subscribe();
                                                                                    })
                                                                                    .thenReturn(ApiResponse.<String>success("Hr created successfully"));
                                                                        })
                                                                )
                                                                .switchIfEmpty(Mono.just(ApiResponse.error("Could not find department with given identifier")))
                                                        )
                                                        .switchIfEmpty(Mono.just(ApiResponse.error("Could not find a job position with the given name")));
                                            });
                                })
                )
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred creating this HR employee {} ", error.getMessage());

                    return Mono.just(ApiResponse.error("Failed to create this HR employee"));

                });
    }

    @Override
    public Mono<ApiResponse<String>> completeEmployeeInformation(Mono<CompleteProfileRequest> completeProfileRequestMono) {

        return completeProfileRequestMono.flatMap(completeProfileRequest ->

                ReactiveSecurityContextHolder
                        .getContext()
                        .flatMap(securityContext -> {

                            String workEmailAddress = securityContext.getAuthentication().getName();

                            return employeeRepository.findByWorkEmailAddressIgnoreCase(workEmailAddress)
                                    .flatMap(employee -> {

                                                LocalDate today = LocalDate.now();

                                                Period age = Period.between(completeProfileRequest.getDateOfBirth(), today);

                                                if (age.getYears() < 18)
                                                    return Mono.just(ApiResponse.<String>error("You should be atleast 18 years old"));

                                                setEmployeeFieldsWithCompleteProfileRequestData(employee, completeProfileRequest);

                                                return employeeRepository
                                                        .save(employee)
                                                        .then(Mono.just(ApiResponse.<String>success("Profile updated successfully")));
                                            }

                                    ).switchIfEmpty(Mono.just(ApiResponse.error("Employee not found with given email")));
                        })

        ).onErrorResume(error -> {

            log.warn("An unexpected error occurred completing profile {} ", error.getMessage());

            return Mono.just(ApiResponse.error("An error occurred completing profile information"));

        });
    }

    @Override
    public Mono<ApiResponse<String>> getProfileCompletionProgress() {

        return ReactiveSecurityContextHolder
                .getContext()
                .flatMap(securityContext -> {

                    String workEmailAddress = securityContext.getAuthentication().getName();

                    return employeeRepository.findByWorkEmailAddressIgnoreCase(workEmailAddress)
                            .map(employee -> ApiResponse.<String>success(String.format("You have completed %.0f%% of your profile", employee.getProfileCompletion())))
                            .switchIfEmpty(Mono.just(ApiResponse.error("Employee with given email not found")));
                })
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred getting profile completion progress {} ", error.getMessage());

                    return Mono.just(ApiResponse.error("An unexpected error occurred fetching profile completion progress"));

                });
    }

    @Override
    public Mono<ApiResponse<EmployeeResponse>> searchEmployeeByWorkEmailAddress(String workEmailAddress) {

        return employeeRepository.findByWorkEmailAddressIgnoreCase(workEmailAddress)
                .flatMap(employee -> departmentRepository.findById(employee.getDepartmentId())
                        .map(department -> {

                            EmployeeResponse employeeResponse = mapToEmployeeResponseFromEmployee(employee, department);

                            return ApiResponse.success("Employee retrieved successfully.", employeeResponse);
                        })
                        .switchIfEmpty(Mono.just(ApiResponse.error("Department with given identifier not found")))
                )
                .switchIfEmpty(Mono.just(ApiResponse.error("Employee with given work email address not found")))
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred searching employee by work email address {} ", error.getMessage());

                    return Mono.just(ApiResponse.error("An unexpected error occurred searching employee by work email address"));

                });
    }

    @Override
    public Mono<ApiResponse<EmployeeProfileResponse>> getEmployeeProfile() {

        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {

            String workEmailAddress = securityContext.getAuthentication().getName();

            String cacheKey = CACHE_KEY_EMPLOYEE_PROFILE_PREFIX + workEmailAddress.toLowerCase();

            return reactiveRedisOperations.opsForValue()
                    .get(cacheKey)
                    .flatMap(obj -> {

                        if (obj instanceof EmployeeProfileResponse cachedProfile) {

                            log.info("Retrieving employee profile from cache {}", workEmailAddress);

                            return Mono.just(ApiResponse.success("Profile retrieved successfully", cachedProfile));

                        }

                        return Mono.empty();

                    })
                    .onErrorResume(error -> {

                        log.warn("Failed to retrieve employee profile from cache: {}", error.getMessage());

                        return Mono.empty();

                    })
                    .switchIfEmpty(employeeRepository.findByWorkEmailAddressIgnoreCase(workEmailAddress)
                            .flatMap(employee -> departmentRepository.findById(employee.getDepartmentId())
                                    .flatMap(department -> {

                                        EmployeeProfileResponse employeeProfileResponse = mapToEmployeeProfileResponseFromEmployee(employee, department);

                                        return reactiveRedisOperations.opsForValue()
                                                .set(cacheKey, employeeProfileResponse, CACHE_TTL)
                                                .doOnError(error -> log.warn("Failed to cache employee profile: {}", error.getMessage()))
                                                .thenReturn(ApiResponse.success("Profile retrieved successfully.", employeeProfileResponse))
                                                .onErrorReturn(ApiResponse.success("Profile retrieved successfully.", employeeProfileResponse));
                                    })
                                    .switchIfEmpty(Mono.just(ApiResponse.error("Department with given identifier not found")))
                            )
                            .switchIfEmpty(Mono.just(ApiResponse.error("Employee with given email not found.")))
                    );
        }).onErrorResume(error -> {

            log.error("An unexpected error occurred getting employee profile {} ", error.getMessage());

            return Mono.just(ApiResponse.error("An unexpected error occurred fetching employee profile."));

        });
    }

    @Override
    public Mono<ApiResponse<List<EmployeeResponse>>> fetchAllEmployees() {

        return reactiveRedisOperations.opsForValue()
                .get(CACHE_KEY_ALL_EMPLOYEES)
                .flatMap(obj -> {

                    if (obj instanceof List<?> cachedList && !cachedList.isEmpty() && cachedList.get(0) instanceof EmployeeResponse) {

                        log.info("Cache hit for all employees");

                        List<EmployeeResponse> employeeResponses = (List<EmployeeResponse>) cachedList;

                        return Mono.just(ApiResponse.success("Employees retrieved successfully.", employeeResponses));

                    } else {

                        log.info("Cache miss for all employees");

                        return Mono.empty();

                    }
                })
                .onErrorResume(error -> {

                    log.info("An unexpected error occurred fetching from redis");

                    return Mono.empty();

                })
                .switchIfEmpty(employeeRepository.findAllByEmploymentStatusNot(EmploymentStatus.TERMINATED)
                        .flatMap(employee -> departmentRepository.findById(employee.getDepartmentId())
                                .map(department -> mapToEmployeeResponseFromEmployee(employee, department))
                                .switchIfEmpty(Mono.error(new ApplicationException("Department not found with given")))
                        )
                        .collectList()
                        .flatMap(employees -> reactiveRedisOperations.opsForValue()
                                .set(CACHE_KEY_ALL_EMPLOYEES, employees, CACHE_TTL)
                                .doOnError(error -> log.error("An unexpected error occurred saving to redis {}", error.getMessage()))
                                .thenReturn(ApiResponse.success("Employees retrieved successfully.", employees))
                                .onErrorReturn(ApiResponse.success("Employees retrieved successfully", employees))
                        )
                )
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred fetching all employees {}", error.getMessage());

                    return Mono.just(ApiResponse.error("An unexpected error occurred fetching all employees"));

                });
    }

    @Override
    public Mono<ApiResponse<String>> terminateEmployee(String id) {

        return employeeRepository.findById(id)
                .flatMap(employee -> {

                    employee.setEmploymentStatus(EmploymentStatus.TERMINATED);

                    employee.setDateUpdated(LocalDateTime.now());

                    return employeeRepository.save(employee)
                            .then(reactiveRedisOperations.delete(CACHE_KEY_ALL_EMPLOYEES))
                            .doOnError(error -> log.error("An unexpected error occurred deleting from redis {}", error.getMessage()))
                            .thenReturn(ApiResponse.<String>success("Employee contract terminated successfully."))
                            .onErrorReturn(ApiResponse.success("Employee contract terminated successfully."));
                })
                .switchIfEmpty(Mono.just(ApiResponse.error("Could not find employee with given identifier")))
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred fetching employee profile {}", error.getMessage());

                    return Mono.just(ApiResponse.success("An unexpected error occurred"));

                });
    }
}
