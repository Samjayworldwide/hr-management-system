package com.samjay.hr_management_system.enumerations;

import lombok.Getter;

@Getter
public enum Role {

    ADMIN_ROLE("ADMIN_ROLE"),

    HR_ROLE("HR_ROLE"),

    EMPLOYEE_ROLE("EMPLOYEE_ROLE");

    private final String roleName;


    Role(String roleName) {

        this.roleName = roleName;

    }
}
