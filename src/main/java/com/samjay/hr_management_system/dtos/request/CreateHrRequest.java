package com.samjay.hr_management_system.dtos.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateHrRequest {

    @NotBlank(message = "Firstname is required")
    @Size(min = 2, max = 100, message = "Firstname must be between 2 and 100 characters")
    private String firstname;

    @NotBlank(message = "Middle name is required")
    @Size(min = 2, max = 100, message = "Middle name must be between 2 and 100 characters")
    private String middleName;

    @NotBlank(message = "Lastname is required")
    @Size(min = 2, max = 100, message = "Lastname must be between 2 and 100 characters")
    private String lastname;

    @NotBlank(message = "Personal email address is required")
    @Email(message = "Please provide a valid email address")
    private String personalEmailAddress;

    @NotBlank(message = "Work email prefix is required")
    private String workEmailPrefix;

    @NotBlank(message = "Job position is required")
    @Size(min = 2, max = 100, message = "Job position must be between 2 and 100 characters")
    private String jobPosition;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Salary must be a valid monetary amount")
    private Double salary;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;
}
