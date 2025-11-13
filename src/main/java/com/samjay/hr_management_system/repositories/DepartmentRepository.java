package com.samjay.hr_management_system.repositories;

import com.samjay.hr_management_system.entities.Department;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface DepartmentRepository extends R2dbcRepository<Department, String> {

    Mono<Department> findByDepartmentNameIgnoreCase(String departmentName);

    Mono<Boolean> existsByDepartmentNameIgnoreCase(String departName);

}