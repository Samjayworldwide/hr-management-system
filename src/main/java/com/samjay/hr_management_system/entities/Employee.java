package com.samjay.hr_management_system.entities;

import com.samjay.hr_management_system.enumerations.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "employees")
public class Employee extends BaseEntity implements Persistable<String> {
    
    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "firstname")
    private String firstname;

    @Column(value = "middle_name")
    private String middleName;

    @Column(value = "lastname")
    private String lastname;

    @Column(value = "full_name")
    private String fullName;

    @Column(value = "personal_email_address")
    private String personalEmailAddress;

    @Column(value = "password")
    private String password;

    @Column(value = "address")
    private String address;

    @Column(value = "city")
    private String city;

    @Column(value = "state")
    private String state;

    @Column(value = "country")
    private String country;

    @Column(value = "work_email_address")
    private String workEmailAddress;

    @Column(value = "job_position")
    private String jobPosition;

    @Column(value = "created_by")
    private String createdBy;

    @Column(value = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(value = "hire_date")
    private LocalDate hireDate;

    @Column(value = "salary")
    private double salary;

    @Column("department_id")
    private String departmentId;

    @Column(value = "role")
    private Role role;

    @Column(value = "gender")
    private Gender gender;

    @Column(value = "marital_status")
    private MaritalStatus maritalStatus;

    @Column(value = "work_type")
    private WorkType workType;

    @Column(value = "employment_status")
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(value = "profile_completion")
    private double profileCompletion;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}