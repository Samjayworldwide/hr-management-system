package com.samjay.hr_management_system.constants;

import java.time.Duration;

public interface Constant {

    String HR_DEPARTMENT = "Human Resource";

    String WORK_EMAIL_SUFFIX = "@hr.management.system.com";

    String CACHE_KEY_ALL_JOB_ROLES = "job_roles:all";

    String CACHE_KEY_JOB_ROLE_PREFIX = "job_role:";

    String CACHE_KEY_ALL_DEPARTMENTS = "departments:all";

    String CACHE_KEY_DEPARTMENT_PREFIX = "department:";

    String CACHE_KEY_EMPLOYEE_PROFILE_PREFIX = "employee:profile:";

    String CACHE_KEY_ALL_EMPLOYEES = "employees:all";

    Duration CACHE_TTL = Duration.ofMinutes(30);

    int MAX_NUMBER_OF_DAYS_FOR_LEAVE_IN_A_YEAR = 21;

    String EMAIL_QUEUE = "email-queue";

    String PAYROLL_QUEUE = "payroll-queue";

    int TOTAL_NUMBER_OF_WORKING_DAYS_IN_A_MONTH = 22;

}
