package com.samjay.hr_management_system.entities;

import com.samjay.hr_management_system.enumerations.GrantLeaveAuthority;
import com.samjay.hr_management_system.enumerations.LeaveType;
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
@Table(name = "leaveRequests")
public class LeaveRequest extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "employee_email_address")
    private String employeeEmailAddress;

    @Column(value = "number_of_leave_days")
    private int numberOfLeaveDays;

    @Column(value = "employee_id")
    private String employeeId;

    @Column(value = "leave_type")
    private LeaveType leaveType;

    @Column(value = "approved_date")
    private LocalDate approvedDate;

    @Column(value = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(value = "is_active")
    private boolean isActive = false;

    @Column(value = "is_approved")
    private boolean isApproved = false;

    @Column(value = "approved_by")
    private String approvedBy;

    @Column("grant_leave_authority")
    private GrantLeaveAuthority grantLeaveAuthority;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}
