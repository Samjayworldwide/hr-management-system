package com.samjay.hr_management_system.dtos.request;

import com.samjay.hr_management_system.enumerations.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLeaveRequest {

    private int numberOfLeaveDays;

    private LeaveType leaveType;
}
