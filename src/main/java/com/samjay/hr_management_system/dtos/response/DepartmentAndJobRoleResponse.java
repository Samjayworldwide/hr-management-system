package com.samjay.hr_management_system.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentAndJobRoleResponse {

    private String departmentName;

    private String departmentShortCode;

    private String officeLocation;

    private List<String> jobRoles;
}
