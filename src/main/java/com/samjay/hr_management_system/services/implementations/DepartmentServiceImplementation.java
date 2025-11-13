package com.samjay.hr_management_system.services.implementations;

import com.samjay.hr_management_system.dtos.request.CreateDepartmentRequest;
import com.samjay.hr_management_system.dtos.response.ApiResponse;
import com.samjay.hr_management_system.entities.Department;
import com.samjay.hr_management_system.repositories.DepartmentRepository;
import com.samjay.hr_management_system.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImplementation implements DepartmentService {

    private final DepartmentRepository departmentRepository;

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

                        .onErrorResume(error -> Mono.just(ApiResponse.error("Failed to create department: " + error.getMessage()))
                        )
        );
    }
}
