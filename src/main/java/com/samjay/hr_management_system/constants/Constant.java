package com.samjay.hr_management_system.constants;

import java.time.Duration;

public interface Constant {

    String HR_DEPARTMENT = "Human Resource";

    String WORK_EMAIL_SUFFIX = "@hr.management.system.com";

    String CACHE_KEY_ALL_JOB_ROLES = "job_roles:all";

    String CACHE_KEY_JOB_ROLE_PREFIX = "job_role:";

    Duration CACHE_TTL = Duration.ofMinutes(30);

}
