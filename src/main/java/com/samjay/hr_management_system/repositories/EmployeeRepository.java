package com.samjay.hr_management_system.repositories;

import com.samjay.hr_management_system.entities.Employee;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface EmployeeRepository extends R2dbcRepository<Employee, String> {

    Mono<Boolean> existsByPersonalEmailAddressIgnoreCase(String personalEmailAddress);

    Mono<Boolean> existsByWorkEmailAddressIgnoreCase(String workEmail);

    Mono<Employee> findByWorkEmailAddressIgnoreCase(String workEmailAddress);

}
