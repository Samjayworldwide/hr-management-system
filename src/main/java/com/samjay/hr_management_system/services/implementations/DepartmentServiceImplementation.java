package com.samjay.hr_management_system.services.implementations;

import com.samjay.hr_management_system.dtos.request.CreateDepartmentRequest;
import com.samjay.hr_management_system.dtos.request.UpdateDepartmentRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.dtos.response.DepartmentAndJobRoleResponse;
import com.samjay.hr_management_system.dtos.response.DepartmentResponse;
import com.samjay.hr_management_system.entities.Department;
import com.samjay.hr_management_system.entities.JobRole;
import com.samjay.hr_management_system.repositories.DepartmentRepository;
import com.samjay.hr_management_system.repositories.JobRoleRepository;
import com.samjay.hr_management_system.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.samjay.hr_management_system.constants.Constant.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImplementation implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final JobRoleRepository jobRoleRepository;

    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;

    @Override
    public Mono<ApiResponse<String>> createDepartment(Mono<CreateDepartmentRequest> createDepartmentRequestMono) {

        return createDepartmentRequestMono.flatMap(createDepartmentRequest ->

                departmentRepository.existsByDepartmentNameIgnoreCase(createDepartmentRequest.getDepartmentName())
                        .flatMap(exists -> {

                            if (exists)
                                return Mono.just(ApiResponse.<String>error("Department already exists"));

                            Department department = new Department();

                            department.setId(UUID.randomUUID().toString());

                            department.setDepartmentName(createDepartmentRequest.getDepartmentName());

                            department.setDepartmentShortCode(createDepartmentRequest.getDepartmentCode());

                            department.setHeadOfDepartment("HOD");

                            department.setOfficeLocation(createDepartmentRequest.getOfficeLocation());

                            department.setNumberOfEmployees(0L);

                            return departmentRepository
                                    .save(department)
                                    .map(savedDepartment -> ApiResponse.<String>success("Department created successfully"));
                        })
                        .onErrorResume(error -> Mono.just(ApiResponse.error("Failed to create department: " + error.getMessage())))
        );
    }

    @Override
    public Mono<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {

        return reactiveRedisOperations
                .opsForValue()
                .get(CACHE_KEY_ALL_DEPARTMENTS)
                .flatMap(obj -> {

                    if (obj instanceof List<?> cachedList && !cachedList.isEmpty() && cachedList.get(0) instanceof DepartmentResponse) {

                        log.info("Cache HIT for all departments");

                        @SuppressWarnings("unchecked")
                        List<DepartmentResponse> departmentResponses = (List<DepartmentResponse>) cachedList;

                        return Mono.just(ApiResponse.success("Departments retrieved successfully", departmentResponses));
                    }

                    return Mono.empty();

                }).onErrorResume(error -> {

                    log.error("Error retrieving from cache: {}", error.getMessage());

                    return Mono.empty();

                }).switchIfEmpty(departmentRepository.findAll()
                        .map(department -> {

                            DepartmentResponse departmentResponse = new DepartmentResponse();

                            departmentResponse.setId(department.getId());

                            departmentResponse.setDepartmentName(department.getDepartmentName());

                            departmentResponse.setDepartmentShortCode(department.getDepartmentShortCode());

                            departmentResponse.setOfficeLocation(department.getOfficeLocation());

                            departmentResponse.setNumberOfEmployees(department.getNumberOfEmployees());

                            return departmentResponse;
                        })
                        .collectList()
                        .flatMap(listOfDepartments -> reactiveRedisOperations
                                .opsForValue()
                                .set(CACHE_KEY_ALL_DEPARTMENTS, listOfDepartments, CACHE_TTL)
                                .doOnError(error -> log.error("Error caching all departments: {}", error.getMessage()))
                                .then(Mono.just(ApiResponse.success("Departments retrieved successfully", listOfDepartments)))
                                .onErrorReturn(ApiResponse.success("Departments retrieved successfully", listOfDepartments))
                        ).onErrorResume(error -> {

                            log.error("Failed to retrieve departments: {}", error.getMessage());

                            return Mono.just(ApiResponse.error("Failed to retrieve departments"));

                        })
                );
    }

    @Override
    public Mono<ApiResponse<DepartmentAndJobRoleResponse>> getADepartmentAndItsJobRoles(String departmentId) {

        String cacheKey = CACHE_KEY_DEPARTMENT_PREFIX + departmentId;

        return reactiveRedisOperations
                .opsForValue()
                .get(cacheKey)
                .flatMap(obj -> {

                    if (obj instanceof DepartmentAndJobRoleResponse departmentAndJobRoleResponse) {

                        log.info("Cache HIT for department and its job roles with ID: {}", departmentId);

                        return Mono.just(ApiResponse.success("Department and its job roles retrieved successfully", departmentAndJobRoleResponse));
                    }
                    return Mono.empty();
                })
                .onErrorResume(error -> {

                    log.error("Error retrieving from cache: {}", error.getMessage());

                    return Mono.empty();
                })
                .switchIfEmpty(departmentRepository.findById(departmentId)
                        .flatMap(department -> jobRoleRepository.findByDepartmentId(department.getId())
                                .map(JobRole::getJobPosition)
                                .collectList()
                                .flatMap(jobRoles -> {

                                    DepartmentAndJobRoleResponse response = new DepartmentAndJobRoleResponse();

                                    response.setDepartmentName(department.getDepartmentName());

                                    response.setDepartmentShortCode(department.getDepartmentShortCode());

                                    response.setOfficeLocation(department.getOfficeLocation());

                                    response.setJobRoles(jobRoles);

                                    return reactiveRedisOperations
                                            .opsForValue()
                                            .set(cacheKey, response, CACHE_TTL)
                                            .doOnError(error -> log.error("Error caching department and its job roles: {}", error.getMessage()))
                                            .then(Mono.just(ApiResponse.success("Department and its job roles retrieved successfully", response)))
                                            .onErrorReturn(ApiResponse.success("Department and its job roles retrieved successfully", response));
                                })
                                .onErrorResume(error -> {

                                    log.error("Failed to retrieve job roles for department ID {}: {}", departmentId, error.getMessage());

                                    return Mono.just(ApiResponse.error("Failed to retrieve job roles for the department"));
                                })
                        )
                        .switchIfEmpty(Mono.just(ApiResponse.error("Department not found")))
                        .onErrorResume(error -> {

                            log.error("Failed to retrieve department and its job roles: {}", error.getMessage());

                            return Mono.just(ApiResponse.error("Failed to retrieve department and its job roles"));
                        })
                );
    }

    @Override
    public Mono<ApiResponse<String>> updateDepartment(String departmentId, Mono<UpdateDepartmentRequest> updateDepartmentRequestMono) {

        String cacheKey = CACHE_KEY_DEPARTMENT_PREFIX + departmentId;

        return updateDepartmentRequestMono
                .flatMap(updateDepartmentRequest -> departmentRepository.findById(departmentId)
                        .flatMap(department -> departmentRepository.existsByDepartmentNameIgnoreCaseAndIdNot(updateDepartmentRequest.getDepartmentName(), departmentId)
                                .flatMap(duplicateExists -> {

                                    if (duplicateExists) {

                                        return Mono.just(ApiResponse.<String>error("Another department with the same name exists"));

                                    }

                                    department.setDepartmentName(updateDepartmentRequest.getDepartmentName());

                                    department.setDepartmentShortCode(updateDepartmentRequest.getDepartmentShortCode());

                                    department.setOfficeLocation(updateDepartmentRequest.getOfficeLocation());

                                    return departmentRepository.save(department)
                                            .flatMap(updatedDepartment -> jobRoleRepository.findByDepartmentId(department.getId())
                                                    .map(JobRole::getJobDescription)
                                                    .collectList()
                                                    .flatMap(jobRoles -> {

                                                        DepartmentAndJobRoleResponse response = new DepartmentAndJobRoleResponse();

                                                        response.setDepartmentName(updatedDepartment.getDepartmentName());

                                                        response.setDepartmentShortCode(updatedDepartment.getDepartmentShortCode());

                                                        response.setOfficeLocation(updatedDepartment.getOfficeLocation());

                                                        response.setJobRoles(jobRoles);

                                                        return reactiveRedisOperations
                                                                .opsForValue()
                                                                .delete(cacheKey)
                                                                .then(reactiveRedisOperations.opsForValue().set(cacheKey, response, CACHE_TTL))
                                                                .then(reactiveRedisOperations.delete(CACHE_KEY_ALL_DEPARTMENTS))
                                                                .doOnError(error -> log.error("Error updating cache for department ID {}: {}", departmentId, error.getMessage()))
                                                                .then(Mono.just(ApiResponse.<String>success("Department updated successfully")))
                                                                .onErrorReturn(ApiResponse.success("Department updated successfully"));
                                                    })
                                            );
                                })
                        )
                        .switchIfEmpty(Mono.just(ApiResponse.error("Department not found")))
                        .onErrorResume(error -> {

                            log.error("Failed to update department: {}", error.getMessage());

                            return Mono.just(ApiResponse.error("Failed to update department"));

                        })
                );
    }

    @Override
    public Mono<ApiResponse<String>> deleteDepartment(String departmentId) {

        String cacheKey = CACHE_KEY_DEPARTMENT_PREFIX + departmentId;

        return departmentRepository.findById(departmentId)
                .flatMap(department -> departmentRepository.delete(department)
                        .then(reactiveRedisOperations.opsForValue().delete(cacheKey))
                        .then(reactiveRedisOperations.delete(CACHE_KEY_ALL_DEPARTMENTS))
                        .doOnError(error -> log.error("Error updating cache after deleting department ID {}: {}", departmentId, error.getMessage()))
                        .then(Mono.just(ApiResponse.<String>success("Department deleted successfully")))
                        .onErrorReturn(ApiResponse.success("Department deleted successfully"))
                ).switchIfEmpty(Mono.just(ApiResponse.error("Department not found")))
                .onErrorResume(error -> {

                    log.error("Failed to delete department: {}", error.getMessage());

                    return Mono.just(ApiResponse.error("Failed to delete department"));
                });
    }
}
