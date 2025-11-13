package com.samjay.hr_management_system.services.implementations;

import com.samjay.hr_management_system.constants.Constant;
import com.samjay.hr_management_system.dtos.request.CreateHrRequest;
import com.samjay.hr_management_system.enumerations.WorkType;
import com.samjay.hr_management_system.utils.Utility;
import com.samjay.hr_management_system.dtos.request.CompleteProfileRequest;
import com.samjay.hr_management_system.dtos.request.CreateEmployeeRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.entities.Employee;
import com.samjay.hr_management_system.enumerations.Role;
import com.samjay.hr_management_system.event.OnboardingEmailEvent;
import com.samjay.hr_management_system.repositories.DepartmentRepository;
import com.samjay.hr_management_system.repositories.EmployeeRepository;
import com.samjay.hr_management_system.repositories.JobRoleRepository;
import com.samjay.hr_management_system.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

import static com.samjay.hr_management_system.constants.Constant.HR_DEPARTMENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImplementation implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final JobRoleRepository jobRoleRepository;

    private final DepartmentRepository departmentRepository;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher applicationEventPublisher;

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

                                                                            String defaultPassword = Utility.generateDefaultPassword();

                                                                            String email = securityContext.getAuthentication().getName();

                                                                            Employee employee = new Employee();

                                                                            employee.setId(UUID.randomUUID().toString());

                                                                            employee.setFirstname(createEmployeeRequest.getFirstname().trim());

                                                                            employee.setMiddleName(createEmployeeRequest.getMiddleName().trim());

                                                                            employee.setLastname(createEmployeeRequest.getLastname().trim());

                                                                            employee.setFullName(createEmployeeRequest.getFirstname().trim() + " " + createEmployeeRequest.getMiddleName().trim() + " " + createEmployeeRequest.getLastname().trim());

                                                                            employee.setPersonalEmailAddress(createEmployeeRequest.getPersonalEmailAddress().trim());

                                                                            employee.setWorkEmailAddress(workEmailAddress.trim());

                                                                            employee.setWorkType(createEmployeeRequest.getWorkType());

                                                                            employee.setPassword(passwordEncoder.encode(defaultPassword));

                                                                            employee.setJobPosition(createEmployeeRequest.getJobPosition());

                                                                            employee.setSalary(createEmployeeRequest.getSalary());

                                                                            employee.setHireDate(createEmployeeRequest.getHireDate());

                                                                            employee.setRole(Role.EMPLOYEE_ROLE);

                                                                            employee.setDepartmentId(department.getId());

                                                                            employee.setCreatedBy(email);

                                                                            double profileCompletion = Utility.calculateCompletion(employee);

                                                                            employee.setProfileCompletion(profileCompletion);

                                                                            Long currentNumberOfEmployees = department.getNumberOfEmployees();

                                                                            department.setNumberOfEmployees(currentNumberOfEmployees + 1);

                                                                            return departmentRepository.save(department)
                                                                                    .flatMap(updatedDepartment -> employeeRepository.save(employee))
                                                                                    .doOnSuccess(savedEmployee -> Mono.fromRunnable(() -> {

                                                                                                OnboardingEmailEvent onboardingEmailEvent = new OnboardingEmailEvent(this,
                                                                                                        createEmployeeRequest.getPersonalEmailAddress(),
                                                                                                        workEmailAddress,
                                                                                                        createEmployeeRequest.getFirstname(),
                                                                                                        defaultPassword);

                                                                                                applicationEventPublisher.publishEvent(onboardingEmailEvent);
                                                                                            })
                                                                                            .subscribeOn(Schedulers.boundedElastic())
                                                                                            .subscribe())
                                                                                    .map(savedEmployee -> ApiResponse.<String>success("Employee saved successfully"));
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

                                                                            String defaultPassword = Utility.generateDefaultPassword();

                                                                            Employee employee = new Employee();

                                                                            employee.setId(UUID.randomUUID().toString());

                                                                            employee.setFirstname(createHrRequest.getFirstname());

                                                                            employee.setMiddleName(createHrRequest.getMiddleName());

                                                                            employee.setLastname(createHrRequest.getLastname());

                                                                            employee.setPassword(passwordEncoder.encode(defaultPassword));

                                                                            employee.setFullName(createHrRequest.getFirstname() + " " + createHrRequest.getMiddleName() + " " + createHrRequest.getLastname());

                                                                            employee.setPersonalEmailAddress(createHrRequest.getPersonalEmailAddress());

                                                                            employee.setJobPosition(createHrRequest.getJobPosition());

                                                                            employee.setWorkEmailAddress(workEmailAddress);

                                                                            employee.setDepartmentId(department.getId());

                                                                            employee.setSalary(createHrRequest.getSalary());

                                                                            employee.setHireDate(createHrRequest.getHireDate());

                                                                            employee.setWorkType(WorkType.ONSITE);

                                                                            employee.setCreatedBy(email);

                                                                            employee.setRole(Role.HR_ROLE);

                                                                            double profileCompletion = Utility.calculateCompletion(employee);

                                                                            employee.setProfileCompletion(profileCompletion);

                                                                            Long currentNumberOfEmployees = department.getNumberOfEmployees();

                                                                            department.setNumberOfEmployees(currentNumberOfEmployees + 1);

                                                                            return departmentRepository.save(department)
                                                                                    .then(employeeRepository.save(employee))
                                                                                    .then(Mono.fromRunnable(() -> {
                                                                                        OnboardingEmailEvent onboardingEmailEvent = new OnboardingEmailEvent(
                                                                                                this,
                                                                                                createHrRequest.getPersonalEmailAddress(),
                                                                                                workEmailAddress,
                                                                                                createHrRequest.getFirstname(),
                                                                                                defaultPassword
                                                                                        );

                                                                                        applicationEventPublisher.publishEvent(onboardingEmailEvent);
                                                                                    }))
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

                                                employee.setAddress(completeProfileRequest.getAddress());

                                                employee.setCity(completeProfileRequest.getCity());

                                                employee.setState(completeProfileRequest.getState());

                                                employee.setCountry(completeProfileRequest.getCountry());

                                                employee.setDateOfBirth(completeProfileRequest.getDateOfBirth());

                                                employee.setGender(completeProfileRequest.getGender());

                                                employee.setMaritalStatus(completeProfileRequest.getMaritalStatus());

                                                double profileCompletion = Utility.calculateCompletion(employee);

                                                employee.setProfileCompletion(profileCompletion);

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
}
