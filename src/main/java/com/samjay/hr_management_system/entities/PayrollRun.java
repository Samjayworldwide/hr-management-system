package com.samjay.hr_management_system.entities;

import com.samjay.hr_management_system.enumerations.PayrollRunStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "payroll_runs")
public class PayrollRun extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "run_date")
    private String runDate;

    @Column(value = "payroll_run_status")
    private PayrollRunStatus payrollRunStatus;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}
