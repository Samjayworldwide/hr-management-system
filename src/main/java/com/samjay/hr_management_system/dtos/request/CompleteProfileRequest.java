package com.samjay.hr_management_system.dtos.request;

import com.samjay.hr_management_system.enumerations.Gender;
import com.samjay.hr_management_system.enumerations.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompleteProfileRequest {

    private String address;

    private String city;

    private String state;

    private String country;

    private LocalDate dateOfBirth;

    private Gender gender;

    private MaritalStatus maritalStatus;

}
