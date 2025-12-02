package com.samjay.hr_management_system.dtos.response;

import com.samjay.hr_management_system.enumerations.GrantLeaveAuthority;
import com.samjay.hr_management_system.enumerations.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActiveLeaveResponse {

    private String employeeId;

    public String employeeEmailAddress;

    private int numberOfLeaveDays;

    private LeaveType leaveType;

    private GrantLeaveAuthority grantLeaveAuthority;

    private LocalDate approvedDate;

    private LocalDate expectedReturnDate;
}
