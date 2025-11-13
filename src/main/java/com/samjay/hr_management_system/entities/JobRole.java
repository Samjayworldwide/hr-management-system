package com.samjay.hr_management_system.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "jobroles")
public class JobRole extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "job_position")
    private String jobPosition;

    @Column(value = "job_description")
    private String jobDescription;

    @Column("department_id")
    private String departmentId;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }

}
