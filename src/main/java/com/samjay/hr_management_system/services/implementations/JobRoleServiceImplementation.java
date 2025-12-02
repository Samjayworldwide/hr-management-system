package com.samjay.hr_management_system.services.implementations;

import com.samjay.hr_management_system.dtos.request.CreateJobRoleRequest;
import com.samjay.hr_management_system.dtos.request.UpdateJobRoleRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.JobRoleResponse;
import com.samjay.hr_management_system.entities.JobRole;
import com.samjay.hr_management_system.globalexception.ApplicationException;
import com.samjay.hr_management_system.repositories.DepartmentRepository;
import com.samjay.hr_management_system.repositories.JobRoleRepository;
import com.samjay.hr_management_system.services.JobRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.samjay.hr_management_system.constants.Constant.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRoleServiceImplementation implements JobRoleService {

    private final JobRoleRepository jobRoleRepository;

    private final DepartmentRepository departmentRepository;

    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;

    @Override
    public Mono<ApiResponse<String>> createJobRole(Mono<CreateJobRoleRequest> createJobRoleRequestMono) {

        return createJobRoleRequestMono.flatMap(createJobRoleRequest ->
                        departmentRepository.findByDepartmentNameIgnoreCase(createJobRoleRequest.getDepartmentName().trim())
                                .flatMap(department ->
                                        jobRoleRepository.existsByJobPositionIgnoreCaseAndDepartmentId(createJobRoleRequest.getJobPosition(), department.getId())
                                                .flatMap(exists -> {

                                                    if (exists)
                                                        return Mono.just(ApiResponse.<String>error("A job position with same name already exists in this department"));

                                                    JobRole jobRole = new JobRole();

                                                    jobRole.setId(UUID.randomUUID().toString());

                                                    jobRole.setJobPosition(createJobRoleRequest.getJobPosition().trim());

                                                    jobRole.setJobDescription(createJobRoleRequest.getJobDescription().trim());

                                                    jobRole.setDepartmentId(department.getId());

                                                    return jobRoleRepository
                                                            .save(jobRole)
                                                            .flatMap(savedJobRole -> {

                                                                JobRoleResponse jobRoleResponse = new JobRoleResponse();

                                                                jobRoleResponse.setId(savedJobRole.getId());

                                                                jobRoleResponse.setJobPosition(savedJobRole.getJobPosition());

                                                                jobRoleResponse.setJobDescription(savedJobRole.getJobDescription());

                                                                jobRoleResponse.setDepartmentName(department.getDepartmentName());

                                                                String cacheKey = CACHE_KEY_JOB_ROLE_PREFIX + savedJobRole.getId();

                                                                log.info("THIS IS THE KEY {}", cacheKey);

                                                                return reactiveRedisOperations
                                                                        .opsForValue()
                                                                        .set(cacheKey, jobRoleResponse, CACHE_TTL)
                                                                        .then(reactiveRedisOperations.delete(CACHE_KEY_ALL_JOB_ROLES))
                                                                        .doOnError(redisError -> log.warn("An unexpected error occurred on redis: {}", redisError.getMessage()))
                                                                        .then(Mono.just(ApiResponse.<String>success("Job role created successfully")))
                                                                        .onErrorReturn(ApiResponse.<String>success("Job role created successfully"));
                                                            });
                                                })
                                )
                                .switchIfEmpty(Mono.just(ApiResponse.error("Department not found"))))

                .onErrorResume(error -> Mono.just(ApiResponse.error("Failed to create job role: " + error.getMessage())));
    }

    @Override
    public Mono<ApiResponse<List<JobRoleResponse>>> fetchAllJobRoles() {

        return reactiveRedisOperations.opsForValue()
                .get(CACHE_KEY_ALL_JOB_ROLES)
                .flatMap(obj -> {
                    if (obj instanceof List<?> cachedList && !cachedList.isEmpty() && cachedList.get(0) instanceof JobRoleResponse) {

                        log.info("Cache HIT for all job roles");

                        @SuppressWarnings("unchecked")
                        List<JobRoleResponse> jobRoleResponses = (List<JobRoleResponse>) cachedList;

                        return Mono.just(ApiResponse.success("Job roles fetched successfully", jobRoleResponses));
                    }

                    return Mono.empty();

                }).onErrorResume(redisError -> {

                    log.warn("An error occurred fetching from redis {}", redisError.getMessage());

                    return Mono.empty();

                }).switchIfEmpty(jobRoleRepository
                        .findAll()
                        .flatMap(jobRole -> departmentRepository.findById(jobRole.getDepartmentId())
                                .map(department -> {

                                    JobRoleResponse response = new JobRoleResponse();

                                    response.setId(jobRole.getId());

                                    response.setJobPosition(jobRole.getJobPosition());

                                    response.setJobDescription(jobRole.getJobDescription());

                                    response.setDepartmentName(department.getDepartmentName());

                                    return response;

                                })
                                .switchIfEmpty(Mono.error(new ApplicationException("Department not found with given")))
                        )
                        .collectList()
                        .flatMap(jobRoles -> reactiveRedisOperations
                                .opsForValue()
                                .set(CACHE_KEY_ALL_JOB_ROLES, jobRoles, CACHE_TTL)
                                .doOnError(redisError -> log.warn("Failed to cache all job roles: {}", redisError.getMessage()))
                                .then(Mono.just(ApiResponse.success("Job roles fetched successfully", jobRoles)))
                                .onErrorReturn(ApiResponse.success("Job roles fetched successfully", jobRoles))
                        )
                )
                .onErrorResume(error -> {

                    log.error("Failed to fetch job roles: {}", error.getMessage());

                    return Mono.just(ApiResponse.error("Failed to fetch job roles."));

                });
    }

    @Override
    public Mono<ApiResponse<JobRoleResponse>> fetchAJobRole(String id) {

        String cacheKey = CACHE_KEY_JOB_ROLE_PREFIX + id;

        return reactiveRedisOperations.opsForValue()
                .get(cacheKey)
                .doOnNext(obj -> log.info("RAW CACHE VALUE for {}: {} [type: {}]", cacheKey, obj, obj != null ? obj.getClass() : "null"))
                .flatMap(obj -> {

                    if (obj instanceof JobRoleResponse jobRoleResponse) {

                        log.info("CACHE HIT for job role: {}", id);

                        return Mono.just(ApiResponse.success("Job role retrieved successfully from cache", jobRoleResponse));

                    } else {

                        log.warn("Cache hit but wrong type! Expected JobRoleResponse, got {}", obj != null ? obj.getClass() : "null");

                        return Mono.empty();

                    }
                })
                .onErrorResume(SerializationException.class, error -> {

                    log.warn("Cache deserialization error for key {}, deleting cache entry: {}", cacheKey, error.getMessage());

                    return reactiveRedisOperations.delete(cacheKey).then(Mono.empty());
                })
                .switchIfEmpty(jobRoleRepository.findById(id)
                        .flatMap(jobRole -> departmentRepository.findById(jobRole.getDepartmentId())
                                .flatMap(department -> {

                                    JobRoleResponse jobRoleResponse = new JobRoleResponse();

                                    jobRoleResponse.setId(jobRole.getId());

                                    jobRoleResponse.setJobPosition(jobRole.getJobPosition());

                                    jobRoleResponse.setJobDescription(jobRole.getJobDescription());

                                    jobRoleResponse.setDepartmentName(department.getDepartmentName());

                                    return reactiveRedisOperations
                                            .opsForValue()
                                            .set(cacheKey, jobRoleResponse, CACHE_TTL)
                                            .doOnError(redisError -> log.warn("Failed to set cache with new value {}", redisError.getMessage()))
                                            .then(Mono.just(ApiResponse.success("Job role retrieved successfully", jobRoleResponse)))
                                            .onErrorReturn(ApiResponse.success("Job role retrieved successfully", jobRoleResponse));
                                })
                                .switchIfEmpty(Mono.just(ApiResponse.error("Department not found")))
                        )
                        .switchIfEmpty(Mono.just(ApiResponse.error("Job role not found")))
                )
                .onErrorResume(error -> {

                    log.error("Error fetching job role {}: {}", id, error.getMessage(), error);

                    return Mono.just(ApiResponse.error("Failed to fetch a job role."));

                });
    }

    @Override
    public Mono<ApiResponse<String>> updateAJobRole(String id, Mono<UpdateJobRoleRequest> updateJobRoleRequestMono) {

        String cacheKey = CACHE_KEY_JOB_ROLE_PREFIX + id;

        return updateJobRoleRequestMono
                .flatMap(updateJobRoleRequest -> jobRoleRepository.findById(id)
                                .flatMap(jobRole -> departmentRepository.findByDepartmentNameIgnoreCase(updateJobRoleRequest.getDepartmentName())
                                                .flatMap(department -> jobRoleRepository.existsByJobPositionIgnoreCaseAndDepartmentIdAndIdNot(
                                                                updateJobRoleRequest.getJobPosition(),
                                                                        department.getId(),
                                                                        id
                                                                )
                                                                .flatMap(duplicateExists -> {

                                                                    if (duplicateExists)
                                                                        return Mono.just(ApiResponse.<String>error("Job position already exists in given department"));

                                                                    jobRole.setJobPosition(updateJobRoleRequest.getJobPosition());

                                                                    jobRole.setJobDescription(updateJobRoleRequest.getJobDescription());

                                                                    jobRole.setDepartmentId(department.getId());

                                                                    jobRole.setDateUpdated(LocalDateTime.now());

                                                                    return jobRoleRepository.save(jobRole)
                                                                            .flatMap(updatedJobRole -> {

                                                                                JobRoleResponse jobRoleResponse = new JobRoleResponse();

                                                                                jobRoleResponse.setId(updatedJobRole.getId());

                                                                                jobRoleResponse.setJobPosition(updatedJobRole.getJobPosition());

                                                                                jobRoleResponse.setJobDescription(updatedJobRole.getJobDescription());

                                                                                jobRoleResponse.setDepartmentName(department.getDepartmentName());

                                                                                return reactiveRedisOperations.delete(cacheKey)
                                                                                        .then(reactiveRedisOperations.opsForValue().set(cacheKey, jobRoleResponse, CACHE_TTL))
                                                                                        .then(reactiveRedisOperations.delete(CACHE_KEY_ALL_JOB_ROLES))
                                                                                        .doOnError(redisError -> log.warn("Failed to update cache for job role {}: {}", updatedJobRole.getId(), redisError.getMessage()))
                                                                                        .then(Mono.just(ApiResponse.<String>success("Job role updated successfully")))
                                                                                        .onErrorReturn(ApiResponse.success("Job role updated successfully"));
                                                                            });
                                                                })
                                                )
                                                .switchIfEmpty(Mono.just(ApiResponse.error("Department not found by given name")))
                                )
                                .switchIfEmpty(Mono.just(ApiResponse.error("Job role not found by given identifier")))

                )
                .onErrorResume(error -> Mono.just(ApiResponse.error("Failed to update job role")));
    }


    @Override
    public Mono<ApiResponse<String>> deleteJobRole(String id) {

        String cacheKey = CACHE_KEY_JOB_ROLE_PREFIX + id;

        return jobRoleRepository.findById(id)
                .flatMap(jobRole -> jobRoleRepository
                        .delete(jobRole)
                        .then(reactiveRedisOperations.delete(cacheKey))
                        .then(reactiveRedisOperations.delete(CACHE_KEY_ALL_JOB_ROLES))
                        .doOnError(redisError -> log.warn("Failed to invalidate global cache after creation: {}", redisError.getMessage()))
                        .then(Mono.just(ApiResponse.<String>success("Job role deleted successfully")))
                        .onErrorReturn(ApiResponse.<String>success("Job role deleted successfully"))
                )
                .switchIfEmpty(Mono.just(ApiResponse.error("Job role not found")))
                .onErrorResume(error -> {

                    log.error("Error deleting job role {}: {}", id, error.getMessage());

                    return Mono.just(ApiResponse.error("Failed to delete job role"));

                });
    }
}