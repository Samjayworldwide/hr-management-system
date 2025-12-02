package com.samjay.hr_management_system.dtos.response;

import com.samjay.hr_management_system.enumerations.GrantLeaveAuthority;
import com.samjay.hr_management_system.enumerations.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaveResponse {

    public String id;

    private String employeeId;

    public String employeeEmailAddress;

    private int numberOfLeaveDays;

    private LeaveType leaveType;

    private GrantLeaveAuthority grantLeaveAuthority;

    private LocalDateTime dateCreated;
}
