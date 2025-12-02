package com.samjay.hr_management_system.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeProfileResponse extends EmployeeResponse {

    private double walletBalance;

}
