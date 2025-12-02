package com.samjay.hr_management_system.dtos.response;

import com.samjay.hr_management_system.enumerations.Gender;
import com.samjay.hr_management_system.enumerations.MaritalStatus;
import com.samjay.hr_management_system.enumerations.WorkType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {

    private String firstname;

    private String middleName;

    private String lastname;

    private String fullName;

    private String personalEmailAddress;

    private String address;

    private String state;

    private String workEmailAddress;

    private String jobPosition;

    private LocalDate dateOfBirth;

    private double salary;

    private String departmentName;

    private Gender gender;

    private MaritalStatus maritalStatus;

    private WorkType workType;
}
