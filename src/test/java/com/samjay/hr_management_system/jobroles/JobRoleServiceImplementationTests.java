
package com.samjay.hr_management_system.jobroles;

import com.samjay.hr_management_system.dtos.request.CreateJobRoleRequest;
import com.samjay.hr_management_system.dtos.request.UpdateJobRoleRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.JobRoleResponse;
import com.samjay.hr_management_system.entities.Department;
import com.samjay.hr_management_system.entities.JobRole;
import com.samjay.hr_management_system.repositories.DepartmentRepository;
import com.samjay.hr_management_system.repositories.JobRoleRepository;
import com.samjay.hr_management_system.services.implementations.JobRoleServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.samjay.hr_management_system.constants.Constant.CACHE_KEY_ALL_JOB_ROLES;
import static com.samjay.hr_management_system.constants.Constant.CACHE_KEY_JOB_ROLE_PREFIX;
import static com.samjay.hr_management_system.constants.Constant.CACHE_TTL;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JobRoleServiceImplementationTests {

    private JobRoleRepository jobRoleRepository;
    private DepartmentRepository departmentRepository;
    private ReactiveRedisOperations<String, Object> redisOps;
    private ReactiveValueOperations<String, Object> valueOps;

    private JobRoleServiceImplementation service;

    @BeforeEach
    void setup() {
        jobRoleRepository = mock(JobRoleRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        redisOps = mock(ReactiveRedisOperations.class);
        valueOps = mock(ReactiveValueOperations.class);

        when(redisOps.opsForValue()).thenReturn(valueOps);

        service = new JobRoleServiceImplementation(jobRoleRepository, departmentRepository, redisOps);
    }

    private Department mockDepartment(String id) {
        Department d = new Department();
        d.setId(id);
        d.setDepartmentName("Engineering");
        d.setNumberOfEmployees(0L);
        return d;
    }

    private JobRole mockJobRole(String id, String deptId, String desc) {
        JobRole jr = new JobRole();
        jr.setId(id);
        jr.setDepartmentId(deptId);
        jr.setJobPosition("Developer");
        jr.setJobDescription(desc);
        jr.setDateUpdated(LocalDateTime.now());
        return jr;
    }

    @Test
    void createJobRole_success() {

        CreateJobRoleRequest req = new CreateJobRoleRequest();
        req.setDepartmentName("Engineering");
        req.setJobPosition("Developer");
        req.setJobDescription("Build stuff");

        String deptId = UUID.randomUUID().toString();
        Department dep = mockDepartment(deptId);

        when(departmentRepository.findByDepartmentNameIgnoreCase("Engineering")).thenReturn(Mono.just(dep));
        when(jobRoleRepository.existsByJobPositionIgnoreCaseAndDepartmentId("Developer", deptId)).thenReturn(Mono.just(false));
        when(jobRoleRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(valueOps.set(startsWith(CACHE_KEY_JOB_ROLE_PREFIX), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));
        when(redisOps.delete(CACHE_KEY_ALL_JOB_ROLES)).thenReturn(Mono.just(1L));

        StepVerifier.create(service.createJobRole(Mono.just(req)))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void createJobRole_conflict_duplicateInDepartment() {

        CreateJobRoleRequest req = new CreateJobRoleRequest();
        req.setDepartmentName("Engineering");
        req.setJobPosition("Developer");
        req.setJobDescription("Build stuff");

        String deptId = UUID.randomUUID().toString();
        Department dep = mockDepartment(deptId);

        when(departmentRepository.findByDepartmentNameIgnoreCase("Engineering")).thenReturn(Mono.just(dep));
        when(jobRoleRepository.existsByJobPositionIgnoreCaseAndDepartmentId("Developer", deptId)).thenReturn(Mono.just(true));

        StepVerifier.create(service.createJobRole(Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("already exists"))
                .verifyComplete();
    }

    @Test
    void createJobRole_error_departmentNotFound() {

        CreateJobRoleRequest req = new CreateJobRoleRequest();
        req.setDepartmentName("Unknown");
        req.setJobPosition("Developer");
        req.setJobDescription("Build stuff");

        when(departmentRepository.findByDepartmentNameIgnoreCase("Unknown")).thenReturn(Mono.empty());

        StepVerifier.create(service.createJobRole(Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("department not found"))
                .verifyComplete();
    }

    @Test
    void fetchAllJobRoles_cacheHit_success() {

        JobRoleResponse resp = new JobRoleResponse();
        resp.setId("jr-1");
        resp.setJobPosition("Developer");
        resp.setJobDescription("Build stuff");
        resp.setDepartmentName("Engineering");

        when(valueOps.get(CACHE_KEY_ALL_JOB_ROLES)).thenReturn(Mono.just(List.of(resp)));

        when(jobRoleRepository.findAll())
                .thenReturn(Flux.empty());

        StepVerifier.create(service.fetchAllJobRoles())
                .expectNextMatches(r -> r.isSuccessful() && r.getResponseBody().size() == 1)
                .verifyComplete();
    }

    @Test
    void fetchAllJobRoles_cacheMiss_dbSuccess() {
        String deptId = UUID.randomUUID().toString();
        JobRole jr = mockJobRole("jr-1", deptId, "Build stuff");
        Department dep = mockDepartment(deptId);

        when(valueOps.get(CACHE_KEY_ALL_JOB_ROLES)).thenReturn(Mono.empty());
        when(jobRoleRepository.findAll()).thenReturn(Flux.just(jr));
        when(departmentRepository.findById(deptId)).thenReturn(Mono.just(dep));
        when(valueOps.set(eq(CACHE_KEY_ALL_JOB_ROLES), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));

        StepVerifier.create(service.fetchAllJobRoles())
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void fetchAJobRole_cacheHit_success() {

        String id = "jr-1";
        String key = CACHE_KEY_JOB_ROLE_PREFIX + id;

        JobRoleResponse cached = new JobRoleResponse();
        cached.setId(id);
        cached.setJobPosition("Developer");
        cached.setJobDescription("Build stuff");
        cached.setDepartmentName("Engineering");

        when(valueOps.get(key)).thenReturn(Mono.just(cached));

        when(jobRoleRepository.findById(id))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.fetchAJobRole(id))
                .expectNextMatches(r -> r.isSuccessful() && "jr-1".equals(r.getResponseBody().getId()))
                .verifyComplete();
    }

    @Test
    void fetchAJobRole_cacheMiss_dbSuccess() {

        String id = "jr-1";
        String deptId = UUID.randomUUID().toString();
        JobRole jr = mockJobRole(id, deptId, "Build stuff");
        Department dep = mockDepartment(deptId);

        when(valueOps.get(CACHE_KEY_JOB_ROLE_PREFIX + id)).thenReturn(Mono.empty());
        when(jobRoleRepository.findById(id)).thenReturn(Mono.just(jr));
        when(departmentRepository.findById(deptId)).thenReturn(Mono.just(dep));
        when(valueOps.set(startsWith(CACHE_KEY_JOB_ROLE_PREFIX), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));

        StepVerifier.create(service.fetchAJobRole(id))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void fetchAJobRole_error_notFound() {

        when(valueOps.get(anyString())).thenReturn(Mono.empty());
        when(jobRoleRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.fetchAJobRole("missing"))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("not found"))
                .verifyComplete();
    }

    @Test
    void updateAJobRole_success() {

        String id = "jr-1";
        String oldDeptId = UUID.randomUUID().toString();
        String newDeptId = UUID.randomUUID().toString();

        JobRole existing = mockJobRole(id, oldDeptId, "Old desc");
        Department newDep = mockDepartment(newDeptId);
        UpdateJobRoleRequest req = new UpdateJobRoleRequest();
        req.setDepartmentName("Engineering");
        req.setJobPosition("Senior Developer");
        req.setJobDescription("New desc");

        when(jobRoleRepository.findById(id)).thenReturn(Mono.just(existing));
        when(departmentRepository.findByDepartmentNameIgnoreCase("Engineering")).thenReturn(Mono.just(newDep));
        when(jobRoleRepository.existsByJobPositionIgnoreCaseAndDepartmentIdAndIdNot("Senior Developer", newDeptId, id)).thenReturn(Mono.just(false));
        when(jobRoleRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(redisOps.delete(CACHE_KEY_JOB_ROLE_PREFIX + id)).thenReturn(Mono.just(1L));
        when(valueOps.set(eq(CACHE_KEY_JOB_ROLE_PREFIX + id), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));
        when(redisOps.delete(CACHE_KEY_ALL_JOB_ROLES)).thenReturn(Mono.just(1L));

        StepVerifier.create(service.updateAJobRole(id, Mono.just(req)))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void updateAJobRole_conflict_duplicateInDepartment() {

        String id = "jr-1";
        String deptId = UUID.randomUUID().toString();

        JobRole existing = mockJobRole(id, deptId, "Desc");
        Department dep = mockDepartment(deptId);
        UpdateJobRoleRequest req = new UpdateJobRoleRequest();
        req.setDepartmentName("Engineering");
        req.setJobPosition("Developer");
        req.setJobDescription("Desc 2");

        when(jobRoleRepository.findById(id)).thenReturn(Mono.just(existing));
        when(departmentRepository.findByDepartmentNameIgnoreCase("Engineering")).thenReturn(Mono.just(dep));
        when(jobRoleRepository.existsByJobPositionIgnoreCaseAndDepartmentIdAndIdNot("Developer", deptId, id)).thenReturn(Mono.just(true));

        StepVerifier.create(service.updateAJobRole(id, Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("already exists"))
                .verifyComplete();
    }

    @Test
    void updateAJobRole_error_departmentNotFound() {

        String id = "jr-1";
        JobRole existing = mockJobRole(id, UUID.randomUUID().toString(), "Desc");
        UpdateJobRoleRequest req = new UpdateJobRoleRequest();
        req.setDepartmentName("Unknown");
        req.setJobPosition("Developer");
        req.setJobDescription("Desc");

        when(jobRoleRepository.findById(id)).thenReturn(Mono.just(existing));
        when(departmentRepository.findByDepartmentNameIgnoreCase("Unknown")).thenReturn(Mono.empty());

        StepVerifier.create(service.updateAJobRole(id, Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("department not found"))
                .verifyComplete();
    }

    @Test
    void updateAJobRole_error_jobRoleNotFound() {

        UpdateJobRoleRequest req = new UpdateJobRoleRequest();
        req.setDepartmentName("Engineering");
        req.setJobPosition("Developer");
        req.setJobDescription("Desc");

        when(jobRoleRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.updateAJobRole("missing", Mono.just(req)))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("job role not found"))
                .verifyComplete();
    }

    @Test
    void deleteJobRole_success() {

        String id = "jr-1";
        JobRole jr = mockJobRole(id, UUID.randomUUID().toString(), "Desc");

        when(jobRoleRepository.findById(id)).thenReturn(Mono.just(jr));
        when(jobRoleRepository.delete(jr)).thenReturn(Mono.empty());
        when(redisOps.delete(CACHE_KEY_JOB_ROLE_PREFIX + id)).thenReturn(Mono.just(1L));
        when(redisOps.delete(CACHE_KEY_ALL_JOB_ROLES)).thenReturn(Mono.just(1L));

        StepVerifier.create(service.deleteJobRole(id))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void deleteJobRole_error_notFound() {

        when(jobRoleRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteJobRole("missing"))
                .expectNextMatches(r -> !r.isSuccessful() && r.getResponseMessage().toLowerCase().contains("not found"))
                .verifyComplete();
    }

    @Test
    void fetchAJobRole_cacheDeserializationError_recoversByDeletingKey() {

        String id = "jr-1";
        String key = CACHE_KEY_JOB_ROLE_PREFIX + id;

        when(valueOps.get(key)).thenReturn(Mono.error(new org.springframework.data.redis.serializer.SerializationException("bad")));
        when(redisOps.delete(key)).thenReturn(Mono.just(1L));

        String deptId = UUID.randomUUID().toString();
        JobRole jr = mockJobRole(id, deptId, "Desc");
        Department dep = mockDepartment(deptId);
        when(jobRoleRepository.findById(id)).thenReturn(Mono.just(jr));
        when(departmentRepository.findById(deptId)).thenReturn(Mono.just(dep));
        when(valueOps.set(eq(key), any(), eq(CACHE_TTL))).thenReturn(Mono.just(true));

        StepVerifier.create(service.fetchAJobRole(id))
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }
}