-- DROP TABLE IF EXISTS jobroles;
-- DROP TABLE IF EXISTS employees;
-- DROP TABLE IF EXISTS departments;

-- DROP TABLE IF EXISTS payroll_records;
-- DROP TABLE IF EXISTS payroll_runs;

CREATE TABLE IF NOT EXISTS departments
(
    id CHAR(36) PRIMARY KEY,
    department_name VARCHAR(255) NOT NULL,
    department_short_code VARCHAR(255) NOT NULL,
    head_of_department VARCHAR(255),
    office_location VARCHAR(255) NOT NULL,
    number_of_employees BIGINT NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS jobroles
(
    id CHAR(36) PRIMARY KEY,
    job_position VARCHAR(255) NOT NULL,
    job_description TEXT,
    department_id CHAR(36) NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_jobrole_department
    FOREIGN KEY (department_id)
    REFERENCES departments(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS employees
(
    id CHAR(36) PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    lastname VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    personal_email_address VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    work_email_address VARCHAR(255) NOT NULL,
    job_position VARCHAR(255) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    hire_date DATE NOT NULL,
    salary DOUBLE PRECISION NOT NULL,
    department_id CHAR(36) NOT NULL,
    role VARCHAR(255) NOT NULL,
    gender VARCHAR(255),
    marital_status VARCHAR(255),
    work_type VARCHAR(255) NOT NULL,
    wallet_balance DOUBLE PRECISION DEFAULT 0.0,
    employment_status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    profile_completion DOUBLE PRECISION,
    number_of_leave_days_left INT,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_department
    FOREIGN KEY (department_id)
    REFERENCES departments(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS leaveRequests
(
    id CHAR(36) PRIMARY KEY,
    employee_email_address VARCHAR(255) NOT NULL,
    number_of_leave_days INT NOT NULL,
    employee_id CHAR(36) NOT NULL,
    leave_type VARCHAR(255) NOT NULL,
    approved_date DATE,
    expected_return_date DATE,
    is_active BIT DEFAULT false,
    is_approved BIT DEFAULT false,
    approved_by VARCHAR(255),
    grant_leave_authority VARCHAR(255) NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP,
    CONSTRAINT fk_employee_leaveRequest
    FOREIGN KEY (employee_id)
    REFERENCES employees(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS payroll_runs
(
    id CHAR(36) PRIMARY KEY,
    run_date VARCHAR(255) NOT NULL,
    payroll_run_status VARCHAR(255) NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payroll_records
(
    id CHAR(36) PRIMARY KEY,
    employee_id CHAR(36) NOT NULL,
    payroll_run_id CHAR(36) NOT NULL,
    gross_pay DOUBLE PRECISION NOT NULL,
    deductions DOUBLE PRECISION NOT NULL,
    net_pay DOUBLE PRECISION NOT NULL,
    payroll_period VARCHAR(255) NOT NULL,
    payroll_record_status VARCHAR(255) NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP,
    CONSTRAINT fk_payroll_employee
    FOREIGN KEY (employee_id)
    REFERENCES employees(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
    CONSTRAINT fk_payroll_run
    FOREIGN KEY (payroll_run_id)
    REFERENCES payroll_runs(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);

INSERT INTO departments
(
    id,
    department_name,
    department_short_code,
    head_of_department,
    office_location,
    number_of_employees,
    date_created,
    date_updated
)
SELECT
    '00000000-0000-0000-0000-000000000001',
    'MANAGEMENT',
    'MGT',
    NULL,
    'Lagos',
    1,
    CURRENT_DATE,
    NULL
    WHERE NOT EXISTS
(
    SELECT 1 FROM departments WHERE department_name = 'MANAGEMENT'
);

INSERT INTO departments
(
    id,
    department_name,
    department_short_code,
    head_of_department,
    office_location,
    number_of_employees,
    date_created,
    date_updated
)
SELECT
    '00000000-0000-0000-0000-000000000002',
    'HUMAN RESOURCE',
    'HR',
    NULL,
    'Lagos',
    0,
    CURRENT_DATE,
    NULL
    WHERE NOT EXISTS
(
    SELECT 1 FROM departments WHERE department_name = 'HUMAN RESOURCE'
);

INSERT INTO employees
(
    id, firstname, middle_name, lastname, full_name,
    personal_email_address, password, address, city, state, country,
    work_email_address, job_position, created_by,
    date_of_birth, hire_date, salary,
    department_id, role, gender, marital_status, work_type, employment_status, profile_completion,
    number_of_leave_days_left
)
SELECT
    '00000000-0000-0000-0000-000000000001',
    'System',
    NULL,
    'Admin',
    'System Admin',
    'mbanisisamuel@yahoo.com',
    '$2a$10$5WdzudhdmqcS/ZHVnBgiLeOO.aAFAplAw1Zg42w5GL8WhtC8Tbpo2',
    'Head Office',
    'Lagos',
    'Lagos',
    'Nigeria',
    'admin@hr.management.system.com',
    'Administrator',
    'System',
    '2000-05-31',
    CURRENT_DATE,
    0.00,
    '00000000-0000-0000-0000-000000000001',
    'ADMIN_ROLE',
    'MALE',
    'SINGLE',
    'ONSITE',
    'ACTIVE',
    NULL,
    NULL
    WHERE NOT EXISTS
(
    SELECT 1 FROM employees WHERE role = 'ADMIN_ROLE'
);