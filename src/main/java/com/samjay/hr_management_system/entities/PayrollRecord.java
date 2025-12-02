package com.samjay.hr_management_system.entities;

import com.samjay.hr_management_system.enumerations.PayrollRecordStatus;
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
@Table(name = "payroll_records")
public class PayrollRecord extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "employee_id")
    private String employeeId;

    @Column(value = "payroll_run_id")
    private String payrollRunId;

    @Column(value = "gross_pay")
    private double grossPay;

    @Column(value = "deductions")
    private double deductions;

    @Column(value = "net_pay")
    private double netPay;

    @Column(value = "payroll_period")
    private String payrollPeriod;

    @Column(value = "payroll_record_status")
    private PayrollRecordStatus payrollRecordStatus;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}
