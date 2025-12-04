package com.samjay.hr_management_system.departments;

import com.samjay.hr_management_system.dtos.request.CreateDepartmentRequest;
import com.samjay.hr_management_system.dtos.request.UpdateDepartmentRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.DepartmentAndJobRoleResponse;
import com.samjay.hr_management_system.dtos.response.DepartmentResponse;
import com.samjay.hr_management_system.entities.Department;
import com.samjay.hr_management_system.entities.JobRole;
import com.samjay.hr_management_system.repositories.DepartmentRepository;
import com.samjay.hr_management_system.repositories.JobRoleRepository;
import com.samjay.hr_management_system.services.implementations.DepartmentServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static com.samjay.hr_management_system.constants.Constant.CACHE_KEY_ALL_DEPARTMENTS;
import static com.samjay.hr_management_system.constants.Constant.CACHE_KEY_DEPARTMENT_PREFIX;
import static com.samjay.hr_management_system.constants.Constant.CACHE_TTL;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DepartmentServiceImplementationTests {

    private DepartmentRepository departmentRepository;
    private JobRoleRepository jobRoleRepository;
    private ReactiveRedisOperations<String, Object> redisOps;
    private ReactiveValueOperations<String, Object> valueOps;

    private DepartmentServiceImplementation service;

    @BeforeEach
    void setup() {

        departmentRepository = mock(DepartmentRepository.class);
        jobRoleRepository = mock(JobRoleRepository.class);
        redisOps = mock(ReactiveRedisOperations.class);
        valueOps = mock(ReactiveValueOperations.class);

        when(redisOps.opsForValue()).thenReturn(valueOps);

        service = new DepartmentServiceImplementation(departmentRepository, jobRoleRepository, redisOps);
    }

    private Department mockDepartment(String id) {

        Department d = new Department();
        d.setId(id);
        d.setDepartmentName("Engineering");
        d.setDepartmentShortCode("ENG");
        d.setOfficeLocation("HQ");
        d.setNumberOfEmployees(5L);
        return d;
    }

    private JobRole mockJobRole(String deptId, String position, String desc) {
        JobRole jr = new JobRole();
        jr.setId(UUID.randomUUID().toString());
        jr.setDepartmentId(deptId);
        jr.setJobPosition(position);
        jr.setJobDescription(desc);
        return jr;
    }

    @Test
    void createDepartment_success() {

        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setDepartmentName("Engineering");
        req.setDepartmentCode("ENG");
        req.setOfficeLocation("HQ");

        when(departmentRepository.existsByDepartmentNameIgnoreCase("Engineering")).thenReturn(Mono.just(false));
        when(departmentRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.createDepartment(Mono.just(req)))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();

        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_conflict_duplicateName() {

        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setDepartmentName("Engineering");

        when(departmentRepository.existsByDepartmentNameIgnoreCase(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(service.createDepartment(Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("already exists"))
                .verifyComplete();
    }

    @Test
    void createDepartment_error_repoFailure() {
        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setDepartmentName("Engineering");

        when(departmentRepository.existsByDepartmentNameIgnoreCase("Engineering")).thenReturn(Mono.error(new RuntimeException("db")));

        StepVerifier.create(service.createDepartment(Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("failed to create"))
                .verifyComplete();
    }

    @Test
    void getAllDepartments_cacheHit_success() {

        DepartmentResponse resp = new DepartmentResponse();
        resp.setId("d-1");
        resp.setDepartmentName("Engineering");

        when(valueOps.get(CACHE_KEY_ALL_DEPARTMENTS)).thenReturn(Mono.just(List.of(resp)));

        when(departmentRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(service.getAllDepartments())
                .expectNextMatches(r -> r.isSuccessful() && r.getResponseBody().size() == 1)
                .verifyComplete();
    }

    @Test
    void getAllDepartments_cacheMiss_dbSuccess() {

        Department dep = mockDepartment("d-1");

        when(valueOps.get(CACHE_KEY_ALL_DEPARTMENTS)).thenReturn(Mono.empty());
        when(departmentRepository.findAll()).thenReturn(Flux.just(dep));
        when(valueOps.set(eq(CACHE_KEY_ALL_DEPARTMENTS), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));

        StepVerifier.create(service.getAllDepartments())
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void getAllDepartments_dbError_returnsError() {

        when(valueOps.get(CACHE_KEY_ALL_DEPARTMENTS)).thenReturn(Mono.empty());
        when(departmentRepository.findAll()).thenReturn(Flux.error(new RuntimeException("db")));

        StepVerifier.create(service.getAllDepartments())
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("failed to retrieve"))
                .verifyComplete();
    }

    @Test
    void getADepartmentAndItsJobRoles_cacheHit_success() {

        String id = "d-1";
        String key = CACHE_KEY_DEPARTMENT_PREFIX + id;
        DepartmentAndJobRoleResponse cached = new DepartmentAndJobRoleResponse();
        cached.setDepartmentName("Engineering");
        cached.setJobRoles(List.of("Developer"));

        when(valueOps.get(key)).thenReturn(Mono.just(cached));

        when(departmentRepository.findById(anyString())).thenReturn(Mono.empty());

        when(jobRoleRepository.findByDepartmentId(anyString())).thenReturn(Flux.empty());

        StepVerifier.create(service.getADepartmentAndItsJobRoles(id))
                .expectNextMatches(r -> r.isSuccessful() && r.getResponseBody().getDepartmentName().equals("Engineering"))
                .verifyComplete();
    }

    @Test
    void getADepartmentAndItsJobRoles_cacheMiss_dbSuccess() {

        String id = "d-1";
        Department dep = mockDepartment(id);
        JobRole jr1 = mockJobRole(id, "Developer", "Build");
        JobRole jr2 = mockJobRole(id, "Tester", "Test");

        when(valueOps.get(CACHE_KEY_DEPARTMENT_PREFIX + id)).thenReturn(Mono.empty());
        when(departmentRepository.findById(id)).thenReturn(Mono.just(dep));
        when(jobRoleRepository.findByDepartmentId(id)).thenReturn(Flux.just(jr1, jr2));
        when(valueOps.set(eq(CACHE_KEY_DEPARTMENT_PREFIX + id), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));

        StepVerifier.create(service.getADepartmentAndItsJobRoles(id))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void getADepartmentAndItsJobRoles_departmentNotFound() {

        when(valueOps.get(anyString())).thenReturn(Mono.empty());
        when(departmentRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.getADepartmentAndItsJobRoles("missing"))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("not found"))
                .verifyComplete();
    }

    @Test
    void getADepartmentAndItsJobRoles_jobRolesFailure_returnsError() {

        String id = "d-1";
        Department dep = mockDepartment(id);

        when(valueOps.get(CACHE_KEY_DEPARTMENT_PREFIX + id)).thenReturn(Mono.empty());
        when(departmentRepository.findById(id)).thenReturn(Mono.just(dep));
        when(jobRoleRepository.findByDepartmentId(id)).thenReturn(Flux.error(new RuntimeException("db")));

        StepVerifier.create(service.getADepartmentAndItsJobRoles(id))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("failed to retrieve job roles"))
                .verifyComplete();
    }

    @Test
    void updateDepartment_success() {

        String id = "d-1";
        Department dep = mockDepartment(id);

        UpdateDepartmentRequest req = new UpdateDepartmentRequest();
        req.setDepartmentName("Engineering");
        req.setDepartmentShortCode("ENG");
        req.setOfficeLocation("HQ");

        JobRole jr = mockJobRole(id, "Developer", "Build");

        when(departmentRepository.findById(id)).thenReturn(Mono.just(dep));
        when(departmentRepository.existsByDepartmentNameIgnoreCaseAndIdNot("Engineering", id)).thenReturn(Mono.just(false));
        when(departmentRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(jobRoleRepository.findByDepartmentId(id)).thenReturn(Flux.just(jr));
        when(valueOps.delete(CACHE_KEY_DEPARTMENT_PREFIX + id)).thenReturn(Mono.just(true));
        when(valueOps.set(eq(CACHE_KEY_DEPARTMENT_PREFIX + id), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));
        when(redisOps.delete(CACHE_KEY_ALL_DEPARTMENTS)).thenReturn(Mono.just(1L));

        StepVerifier.create(service.updateDepartment(id, Mono.just(req)))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void updateDepartment_conflict_duplicateName() {

        String id = "d-1";
        Department dep = mockDepartment(id);
        UpdateDepartmentRequest req = new UpdateDepartmentRequest();
        req.setDepartmentName("Engineering");

        when(departmentRepository.findById(id)).thenReturn(Mono.just(dep));
        when(departmentRepository.existsByDepartmentNameIgnoreCaseAndIdNot("Engineering", id)).thenReturn(Mono.just(true));

        StepVerifier.create(service.updateDepartment(id, Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("same name exists"))
                .verifyComplete();
    }

    @Test
    void updateDepartment_notFound() {

        UpdateDepartmentRequest req = new UpdateDepartmentRequest();
        req.setDepartmentName("Engineering");

        when(departmentRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.updateDepartment("missing", Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("not found"))
                .verifyComplete();
    }

    @Test
    void updateDepartment_error_repoFailure_returnsError() {

        String id = "d-1";
        UpdateDepartmentRequest req = new UpdateDepartmentRequest();
        req.setDepartmentName("Engineering");

        when(departmentRepository.findById(id)).thenReturn(Mono.error(new RuntimeException("db")));

        StepVerifier.create(service.updateDepartment(id, Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("failed to update"))
                .verifyComplete();
    }

    @Test
    void deleteDepartment_success() {

        String id = "d-1";
        Department dep = mockDepartment(id);

        when(departmentRepository.findById(id)).thenReturn(Mono.just(dep));
        when(departmentRepository.delete(dep)).thenReturn(Mono.empty());
        when(valueOps.delete(CACHE_KEY_DEPARTMENT_PREFIX + id)).thenReturn(Mono.just(true));
        when(redisOps.delete(CACHE_KEY_ALL_DEPARTMENTS)).thenReturn(Mono.just(1L));

        StepVerifier.create(service.deleteDepartment(id))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void deleteDepartment_notFound() {

        when(departmentRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteDepartment("missing"))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("not found"))
                .verifyComplete();
    }

    @Test
    void deleteDepartment_error_repoFailure_returnsError() {

        String id = "d-1";
        when(departmentRepository.findById(id)).thenReturn(Mono.error(new RuntimeException("db")));

        StepVerifier.create(service.deleteDepartment(id))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("failed to delete"))
                .verifyComplete();
    }
}