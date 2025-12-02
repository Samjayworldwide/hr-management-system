package com.samjay.hr_management_system.repositories;

import com.samjay.hr_management_system.entities.Employee;
import com.samjay.hr_management_system.enumerations.EmploymentStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface EmployeeRepository extends R2dbcRepository<Employee, String> {

    Mono<Boolean> existsByPersonalEmailAddressIgnoreCase(String personalEmailAddress);

    Mono<Boolean> existsByWorkEmailAddressIgnoreCase(String workEmail);

    Mono<Employee> findByWorkEmailAddressIgnoreCase(String workEmailAddress);

    Flux<Employee> findByEmploymentStatusNot(EmploymentStatus employmentStatus);

    Flux<Employee> findAllByEmploymentStatusNot(EmploymentStatus employmentStatus);

}
