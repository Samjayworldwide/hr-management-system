package com.samjay.hr_management_system.repositories;

import com.samjay.hr_management_system.entities.JobRole;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface JobRoleRepository extends R2dbcRepository<JobRole, String> {

    Mono<JobRole> findByJobPositionIgnoreCase(String jobPosition);

    Mono<Boolean> existsByJobPositionIgnoreCaseAndDepartmentId(String jobPosition, String departmentId);

    Mono<Boolean> existsByJobPositionIgnoreCaseAndDepartmentIdAndIdNot(String jobPosition, String departmentId, String id);

    Flux<JobRole> findByDepartmentId(String departmentId);

}
