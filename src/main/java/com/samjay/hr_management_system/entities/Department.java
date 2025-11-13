package com.samjay.hr_management_system.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "departments")
public class Department extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "department_name")
    private String departmentName;

    @Column(value = "department_short_code")
    private String departmentShortCode;

    @Column(value = "head_of_department")
    private String headOfDepartment;

    @Column(value = "office_location")
    private String officeLocation;

    @Column(value = "number_of_employees")
    private Long numberOfEmployees;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}
