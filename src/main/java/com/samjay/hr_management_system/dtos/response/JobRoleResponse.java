package com.samjay.hr_management_system.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobRoleResponse {

    private String id;

    private String jobPosition;

    private String jobDescription;

    private String departmentName;
}
