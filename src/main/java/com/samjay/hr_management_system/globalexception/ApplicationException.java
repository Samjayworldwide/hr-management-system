package com.samjay.hr_management_system.globalexception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class ApplicationException extends RuntimeException {

    public ApplicationException(String message) {

        super(message);
    }
}
