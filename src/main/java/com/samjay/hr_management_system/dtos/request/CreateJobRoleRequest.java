package com.samjay.hr_management_system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateJobRoleRequest {

    @NotBlank(message = "Job position is required")
    @Size(min = 10, max = 100, message = "Job position must be between 10 and 100 characters")
    private String jobPosition;

    @NotBlank(message = "job description is required")
    @Size(min = 50, max = 200, message = "Job description must be between 50 and 100 characters")
    private String jobDescription;

    @NotBlank(message = "Department name is required")
    @Size(min = 10, max = 50, message = "Department name must be between 10 and 50 characters")
    private String departmentName;
}
