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
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String departmentName;

    @NotBlank(message = "Department short code is required")
    @Size(min = 3, max = 3, message = "Department code must be exactly 3 characters")
    private String departmentCode;

    @NotBlank(message = "Office location is required")
    @Size(min = 20, max = 100, message = "Office location must be between 20 and 100 characters")
    private String officeLocation;
}
