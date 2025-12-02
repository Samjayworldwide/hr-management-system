package com.samjay.hr_management_system.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentResponse {

    private String id;

    private String departmentName;

    private String departmentShortCode;

    private String officeLocation;

    private Long numberOfEmployees;
}
