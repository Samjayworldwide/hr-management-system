package com.samjay.hr_management_system.employees;

import com.samjay.hr_management_system.dtos.request.CompleteProfileRequest;
import com.samjay.hr_management_system.dtos.request.CreateEmployeeRequest;
import com.samjay.hr_management_system.dtos.request.CreateHrRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.EmployeeProfileResponse;
import com.samjay.hr_management_system.dtos.response.EmployeeResponse;
import com.samjay.hr_management_system.entities.Department;
import com.samjay.hr_management_system.entities.Employee;
import com.samjay.hr_management_system.entities.JobRole;
import com.samjay.hr_management_system.enumerations.EmploymentStatus;
import com.samjay.hr_management_system.enumerations.Gender;
import com.samjay.hr_management_system.enumerations.MaritalStatus;
import com.samjay.hr_management_system.enumerations.Role;
import com.samjay.hr_management_system.enumerations.WorkType;
import com.samjay.hr_management_system.publishers.EmailPublisher;
import com.samjay.hr_management_system.repositories.DepartmentRepository;
import com.samjay.hr_management_system.repositories.EmployeeRepository;
import com.samjay.hr_management_system.repositories.JobRoleRepository;
import com.samjay.hr_management_system.services.implementations.EmployeeServiceImplementation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.AdditionalAnswers;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.samjay.hr_management_system.constants.Constant.CACHE_KEY_ALL_EMPLOYEES;
import static com.samjay.hr_management_system.constants.Constant.CACHE_TTL;
import static com.samjay.hr_management_system.constants.Constant.CACHE_KEY_EMPLOYEE_PROFILE_PREFIX;
import static com.samjay.hr_management_system.constants.Constant.HR_DEPARTMENT;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplementationTests {

    private EmployeeRepository employeeRepository;
    private JobRoleRepository jobRoleRepository;
    private DepartmentRepository departmentRepository;
    private ReactiveRedisOperations<String, Object> reactiveRedisOperations;
    private ReactiveValueOperations<String, Object> valueOps;

    private EmployeeServiceImplementation service;

    @BeforeEach
    void setup() {

        employeeRepository = mock(EmployeeRepository.class);
        jobRoleRepository = mock(JobRoleRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        EmailPublisher emailPublisher = mock(EmailPublisher.class);
        reactiveRedisOperations = mock(ReactiveRedisOperations.class);
        valueOps = mock(ReactiveValueOperations.class);

        when(reactiveRedisOperations.opsForValue()).thenReturn(valueOps);
        when(passwordEncoder.encode(anyString())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(emailPublisher.queueEmail(any())).thenReturn(Mono.empty());

        service = new EmployeeServiceImplementation(
                employeeRepository, jobRoleRepository, departmentRepository,
                passwordEncoder, emailPublisher, reactiveRedisOperations
        );
    }

    private JobRole mockJobRole(String deptId, String jobPosition) {

        JobRole jr = new JobRole();
        jr.setId(UUID.randomUUID().toString());
        jr.setDepartmentId(deptId);
        jr.setJobPosition(jobPosition);
        return jr;
    }

    private Department mockDepartment(String id, String name) {

        Department d = new Department();
        d.setId(id);
        d.setDepartmentName(name);
        d.setNumberOfEmployees(0L);
        return d;
    }

    private Employee mockEmployee(String deptId, String workEmail) {

        Employee e = new Employee();
        e.setId(UUID.randomUUID().toString());
        e.setFirstname("John");
        e.setMiddleName("M");
        e.setLastname("Doe");
        e.setFullName("John M Doe");
        e.setPersonalEmailAddress("john.personal@example.com");
        e.setWorkEmailAddress(workEmail);
        e.setDepartmentId(deptId);
        e.setJobPosition("Developer");
        e.setSalary(1000.0);
        e.setHireDate(LocalDate.now());
        e.setWorkType(WorkType.ONSITE);
        e.setRole(Role.EMPLOYEE_ROLE);
        e.setEmploymentStatus(EmploymentStatus.ACTIVE);
        e.setProfileCompletion(50.0);
        return e;
    }

    @Test
    void createNewEmployee_success() {

        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setFirstname("John");
        req.setMiddleName("M");
        req.setLastname("Doe");
        req.setPersonalEmailAddress("johndoe@gmail.com");
        req.setWorkEmailPrefix("john.doe");
        req.setJobPosition("Developer");
        req.setSalary(1000.0);
        req.setHireDate(LocalDate.now());
        req.setWorkType(WorkType.ONSITE);

        when(employeeRepository.existsByPersonalEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(false));
        when(employeeRepository.existsByWorkEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(false));

        String deptId = UUID.randomUUID().toString();
        JobRole jr = mockJobRole(deptId, "Developer");
        Department dep = mockDepartment(deptId, "Engineering");

        when(jobRoleRepository.findByJobPositionIgnoreCase("Developer")).thenReturn(Mono.just(jr));
        when(departmentRepository.findById(jr.getDepartmentId())).thenReturn(Mono.just(dep));
        when(departmentRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(employeeRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(reactiveRedisOperations.delete(CACHE_KEY_ALL_EMPLOYEES)).thenReturn(Mono.just(1L));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<String>> result = service.createNewEmployee(Mono.just(req))
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));


        StepVerifier.create(result)
                .assertNext(r -> {
                    System.out.println("DEBUG RESPONSE: " + r.getResponseMessage());
                    assertTrue(r.isSuccessful());
                    assertTrue(r.getResponseMessage().contains("saved successfully"));
                })
                .verifyComplete();
    }


    @Test
    void createNewEmployee_conflict_personalEmail() {

        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setPersonalEmailAddress("exists@gmail.com");
        req.setWorkEmailPrefix("john");

        when(employeeRepository.existsByPersonalEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(true));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<String>> result = service.createNewEmployee(Mono.just(req))
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("already exists"))
                .verifyComplete();
    }

    @Test
    void createHR_success_inHRDepartment() {

        CreateHrRequest req = new CreateHrRequest();
        req.setFirstname("Alice");
        req.setMiddleName("K");
        req.setLastname("Smith");
        req.setPersonalEmailAddress("alice@gmail.com");
        req.setWorkEmailPrefix("alice.smith");
        req.setJobPosition("HR Manager");
        req.setSalary(1500.0);
        req.setHireDate(LocalDate.now());

        when(employeeRepository.existsByPersonalEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(false));
        when(employeeRepository.existsByWorkEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(false));

        String deptId = UUID.randomUUID().toString();
        JobRole jr = mockJobRole(deptId, "HR Manager");
        Department dep = mockDepartment(deptId, HR_DEPARTMENT);

        when(jobRoleRepository.findByJobPositionIgnoreCase("HR Manager")).thenReturn(Mono.just(jr));
        when(departmentRepository.findById(deptId)).thenReturn(Mono.just(dep));
        when(departmentRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(employeeRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<String>> result = service.createHR(Mono.just(req))
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void createHR_error_notHRDepartment() {

        CreateHrRequest req = new CreateHrRequest();
        req.setPersonalEmailAddress("alice@gmail.com");
        req.setWorkEmailPrefix("alice.smith");
        req.setJobPosition("HR Manager");

        String deptId = UUID.randomUUID().toString();
        JobRole jr = mockJobRole(deptId, "HR Manager");
        Department dep = mockDepartment(deptId, "Engineering");

        when(employeeRepository.existsByPersonalEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(false));
        when(employeeRepository.existsByWorkEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(false));
        when(jobRoleRepository.findByJobPositionIgnoreCase("HR Manager")).thenReturn(Mono.just(jr));
        when(departmentRepository.findById(deptId)).thenReturn(Mono.just(dep));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<String>> result = service.createHR(Mono.just(req))
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("only add employees in the hr"))
                .verifyComplete();
    }

    @Test
    void completeEmployeeInformation_success() {

        Employee e = mockEmployee(UUID.randomUUID().toString(), "john.doe@company.com");
        when(employeeRepository.findByWorkEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(e));
        when(employeeRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        CompleteProfileRequest req = new CompleteProfileRequest();
        req.setAddress("123 Main St");
        req.setCity("City");
        req.setState("State");
        req.setCountry("Country");
        req.setDateOfBirth(LocalDate.now().minusYears(25));
        req.setGender(Gender.MALE);
        req.setMaritalStatus(MaritalStatus.SINGLE);

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<String>> result = service.completeEmployeeInformation(Mono.just(req))
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void completeEmployeeInformation_underAge_error() {

        Employee e = mockEmployee(UUID.randomUUID().toString(), "john.doe@company.com");
        when(employeeRepository.findByWorkEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(e));

        CompleteProfileRequest req = new CompleteProfileRequest();
        req.setDateOfBirth(LocalDate.now().minusYears(16));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<String>> result = service.completeEmployeeInformation(Mono.just(req))
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("atleast 18"))
                .verifyComplete();
    }

    @Test
    void getProfileCompletionProgress_success() {

        var e = mockEmployee(UUID.randomUUID().toString(), "john.doe@company.com");
        e.setProfileCompletion(70.0);
        when(employeeRepository.findByWorkEmailAddressIgnoreCase(anyString())).thenReturn(Mono.just(e));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<String>> result = service.getProfileCompletionProgress()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(r -> r.isSuccessful() && r.getResponseMessage().contains("70"))
                .verifyComplete();
    }

    @Test
    void searchEmployeeByWorkEmailAddress_success() {

        var e = mockEmployee(UUID.randomUUID().toString(), "john.doe@company.com");
        when(employeeRepository.findByWorkEmailAddressIgnoreCase("john.doe@company.com")).thenReturn(Mono.just(e));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<EmployeeResponse>> result = service.searchEmployeeByWorkEmailAddress("john.doe@company.com")
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void getEmployeeProfile_cacheHit_success() {

        String key = CACHE_KEY_EMPLOYEE_PROFILE_PREFIX + "john.doe@company.com";
        EmployeeProfileResponse cached = new EmployeeProfileResponse();
        cached.setFirstname("John");

        when(valueOps.get(key)).thenReturn(Mono.just(cached));

        when(employeeRepository.findByWorkEmailAddressIgnoreCase(anyString()))
                .thenReturn(Mono.just(mockEmployee("dept123", "john.doe@company.com")));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<EmployeeProfileResponse>> result = service.getEmployeeProfile()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(r -> r.isSuccessful() && r.getResponseBody().getFirstname().equals("John"))
                .verifyComplete();
    }

    @Test
    void getEmployeeProfile_cacheMiss_dbSuccess() {

        String deptId = UUID.randomUUID().toString();
        Employee e = mockEmployee(deptId, "john.doe@company.com");
        Department d = mockDepartment(deptId, "Engineering");

        when(valueOps.get(anyString())).thenReturn(Mono.empty());
        when(employeeRepository.findByWorkEmailAddressIgnoreCase("john.doe@company.com")).thenReturn(Mono.just(e));
        when(departmentRepository.findById(deptId)).thenReturn(Mono.just(d));
        when(valueOps.set(anyString(), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<EmployeeProfileResponse>> result = service.getEmployeeProfile()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void fetchAllEmployees_cacheHit_success() {

        EmployeeResponse employeeResponse = new EmployeeResponse();
        employeeResponse.setFirstname("John");
        employeeResponse.setLastname("Doe");

        List<EmployeeResponse> cached = List.of(employeeResponse);
        when(valueOps.get(CACHE_KEY_ALL_EMPLOYEES)).thenReturn(Mono.just(cached));

        when(employeeRepository.findAllByEmploymentStatusNot(any()))
                .thenReturn(Flux.empty());

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<List<EmployeeResponse>>> result = service.fetchAllEmployees()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void fetchAllEmployees_cacheMiss_dbSuccess() {

        Employee e = mockEmployee(UUID.randomUUID().toString(), "john@company.com");
        Department d = mockDepartment(e.getDepartmentId(), "Engineering");

        when(valueOps.get(CACHE_KEY_ALL_EMPLOYEES)).thenReturn(Mono.empty());
        when(employeeRepository.findAllByEmploymentStatusNot(any())).thenReturn(reactor.core.publisher.Flux.just(e));
        when(departmentRepository.findById(e.getDepartmentId())).thenReturn(Mono.just(d));
        when(valueOps.set(eq(CACHE_KEY_ALL_EMPLOYEES), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));

        var auth = new TestingAuthenticationToken("john.doe@company.com", "pwd", List.of(() -> "ADMIN_ROLE"));
        SecurityContext securityContext = new SecurityContextImpl(auth);

        Mono<ApiResponse<List<EmployeeResponse>>> result = service.fetchAllEmployees()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

        StepVerifier.create(result)
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void terminateEmployee_success() {

        Employee e = mockEmployee(UUID.randomUUID().toString(), "john@company.com");
        Assertions.assertNotNull(e.getId());
        when(employeeRepository.findById(e.getId())).thenReturn(Mono.just(e));
        when(employeeRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(reactiveRedisOperations.delete(CACHE_KEY_ALL_EMPLOYEES)).thenReturn(Mono.just(1L));

        StepVerifier.create(service.terminateEmployee(e.getId()))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        Employee saved = captor.getValue();
        assert saved.getEmploymentStatus() == EmploymentStatus.TERMINATED;
        assert saved.getDateUpdated() != null && saved.getDateUpdated().isBefore(LocalDateTime.now().plusSeconds(1));
    }
}
