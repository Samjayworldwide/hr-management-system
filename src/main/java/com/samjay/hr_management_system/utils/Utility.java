package com.samjay.hr_management_system.utils;

import com.samjay.hr_management_system.dtos.request.*;
import com.samjay.hr_management_system.dtos.response.*;
import com.samjay.hr_management_system.entities.*;
import com.samjay.hr_management_system.enumerations.GrantLeaveAuthority;
import com.samjay.hr_management_system.enumerations.PayrollRecordStatus;
import com.samjay.hr_management_system.enumerations.Role;
import com.samjay.hr_management_system.enumerations.WorkType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Utility {

    private Utility() {
    }

    public static Employee mapToEmployeeFromCreateEmployeeRequest(CreateEmployeeRequest createEmployeeRequest, String workEmailAddress, String hashedPassword, String email, Department department) {

        Employee employee = new Employee();

        employee.setId(UUID.randomUUID().toString());

        employee.setFirstname(createEmployeeRequest.getFirstname().trim());

        employee.setMiddleName(createEmployeeRequest.getMiddleName().trim());

        employee.setLastname(createEmployeeRequest.getLastname().trim());

        employee.setFullName(createEmployeeRequest.getFirstname().trim() + " " + createEmployeeRequest.getMiddleName().trim() + " " + createEmployeeRequest.getLastname().trim());

        employee.setPersonalEmailAddress(createEmployeeRequest.getPersonalEmailAddress().trim());

        employee.setWorkEmailAddress(workEmailAddress.trim());

        employee.setWorkType(createEmployeeRequest.getWorkType());

        employee.setPassword(hashedPassword);

        employee.setJobPosition(createEmployeeRequest.getJobPosition());

        employee.setSalary(createEmployeeRequest.getSalary());

        employee.setHireDate(createEmployeeRequest.getHireDate());

        employee.setRole(Role.EMPLOYEE_ROLE);

        employee.setDepartmentId(department.getId());

        employee.setCreatedBy(email);

        double profileCompletion = calculateCompletion(employee);

        employee.setProfileCompletion(profileCompletion);

        return employee;

    }

    public static Employee mapToEmployeeFromCreateHrRequest(CreateHrRequest createHrRequest, String hashedPassword, String workEmailAddress, String email, Department department) {

        Employee employee = new Employee();

        employee.setId(UUID.randomUUID().toString());

        employee.setFirstname(createHrRequest.getFirstname());

        employee.setMiddleName(createHrRequest.getMiddleName());

        employee.setLastname(createHrRequest.getLastname());

        employee.setPassword(hashedPassword);

        employee.setFullName(createHrRequest.getFirstname() + " " + createHrRequest.getMiddleName() + " " + createHrRequest.getLastname());

        employee.setPersonalEmailAddress(createHrRequest.getPersonalEmailAddress());

        employee.setJobPosition(createHrRequest.getJobPosition());

        employee.setWorkEmailAddress(workEmailAddress);

        employee.setDepartmentId(department.getId());

        employee.setSalary(createHrRequest.getSalary());

        employee.setHireDate(createHrRequest.getHireDate());

        employee.setWorkType(WorkType.ONSITE);

        employee.setCreatedBy(email);

        employee.setRole(Role.HR_ROLE);

        double profileCompletion = calculateCompletion(employee);

        employee.setProfileCompletion(profileCompletion);

        return employee;

    }

    public static void setEmployeeFieldsWithCompleteProfileRequestData(Employee employee, CompleteProfileRequest completeProfileRequest) {

        employee.setAddress(completeProfileRequest.getAddress());

        employee.setCity(completeProfileRequest.getCity());

        employee.setState(completeProfileRequest.getState());

        employee.setCountry(completeProfileRequest.getCountry());

        employee.setDateOfBirth(completeProfileRequest.getDateOfBirth());

        employee.setGender(completeProfileRequest.getGender());

        employee.setMaritalStatus(completeProfileRequest.getMaritalStatus());

        double profileCompletion = calculateCompletion(employee);

        employee.setProfileCompletion(profileCompletion);

    }

    public static EmployeeResponse mapToEmployeeResponseFromEmployee(Employee employee, Department department) {

        EmployeeResponse employeeResponse = new EmployeeResponse();

        employeeResponse.setFirstname(employee.getFirstname());

        employeeResponse.setMiddleName(employee.getMiddleName());

        employeeResponse.setLastname(employee.getLastname());

        employeeResponse.setFullName(employee.getFullName());

        employeeResponse.setPersonalEmailAddress(employee.getPersonalEmailAddress());

        employeeResponse.setAddress(employee.getAddress());

        employeeResponse.setState(employee.getState());

        employeeResponse.setWorkEmailAddress(employee.getWorkEmailAddress());

        employeeResponse.setJobPosition(employee.getJobPosition());

        employeeResponse.setDateOfBirth(employee.getDateOfBirth());

        employeeResponse.setSalary(employee.getSalary());

        employeeResponse.setDepartmentName(department.getDepartmentName());

        employeeResponse.setGender(employee.getGender());

        employeeResponse.setMaritalStatus(employee.getMaritalStatus());

        employeeResponse.setWorkType(employee.getWorkType());

        return employeeResponse;
    }

    public static EmployeeProfileResponse mapToEmployeeProfileResponseFromEmployee(Employee employee, Department department) {

        EmployeeProfileResponse employeeProfileResponse = new EmployeeProfileResponse();

        employeeProfileResponse.setFirstname(employee.getFirstname());

        employeeProfileResponse.setMiddleName(employee.getMiddleName());

        employeeProfileResponse.setLastname(employee.getLastname());

        employeeProfileResponse.setFullName(employee.getFullName());

        employeeProfileResponse.setPersonalEmailAddress(employee.getPersonalEmailAddress());

        employeeProfileResponse.setAddress(employee.getAddress());

        employeeProfileResponse.setState(employee.getState());

        employeeProfileResponse.setWorkEmailAddress(employee.getWorkEmailAddress());

        employeeProfileResponse.setJobPosition(employee.getJobPosition());

        employeeProfileResponse.setDateOfBirth(employee.getDateOfBirth());

        employeeProfileResponse.setSalary(employee.getSalary());

        employeeProfileResponse.setDepartmentName(department.getDepartmentName());

        employeeProfileResponse.setGender(employee.getGender());

        employeeProfileResponse.setMaritalStatus(employee.getMaritalStatus());

        employeeProfileResponse.setWorkType(employee.getWorkType());

        employeeProfileResponse.setWalletBalance(employee.getWalletBalance());

        return employeeProfileResponse;
    }

    public static Department mapToDepartmentFromCreateDepartmentRequest(CreateDepartmentRequest createDepartmentRequest) {

        Department department = new Department();

        department.setId(UUID.randomUUID().toString());

        department.setDepartmentName(createDepartmentRequest.getDepartmentName());

        department.setDepartmentShortCode(createDepartmentRequest.getDepartmentCode());

        department.setHeadOfDepartment("");

        department.setOfficeLocation(createDepartmentRequest.getOfficeLocation());

        department.setNumberOfEmployees(0L);

        return department;
    }

    public static DepartmentResponse mapToDepartmentResponseFromDepartment(Department department) {

        DepartmentResponse departmentResponse = new DepartmentResponse();

        departmentResponse.setId(department.getId());

        departmentResponse.setDepartmentName(department.getDepartmentName());

        departmentResponse.setDepartmentShortCode(department.getDepartmentShortCode());

        departmentResponse.setOfficeLocation(department.getOfficeLocation());

        departmentResponse.setNumberOfEmployees(department.getNumberOfEmployees());

        return departmentResponse;
    }

    public static DepartmentAndJobRoleResponse mapToDepartmentAndJobRoleResponseFromDepartment(Department department, List<String> jobRoles) {

        DepartmentAndJobRoleResponse response = new DepartmentAndJobRoleResponse();

        response.setDepartmentName(department.getDepartmentName());

        response.setDepartmentShortCode(department.getDepartmentShortCode());

        response.setOfficeLocation(department.getOfficeLocation());

        response.setJobRoles(jobRoles);

        return response;
    }

    public static JobRole mapToJobRoleFromCreateJobRoleRequest(CreateJobRoleRequest createJobRoleRequest, Department department) {

        JobRole jobRole = new JobRole();

        jobRole.setId(UUID.randomUUID().toString());

        jobRole.setJobPosition(createJobRoleRequest.getJobPosition().trim());

        jobRole.setJobDescription(createJobRoleRequest.getJobDescription().trim());

        jobRole.setDepartmentId(department.getId());

        return jobRole;
    }

    public static JobRoleResponse mapToJobRoleResponseFromJobRole(JobRole jobRole, Department department) {

        JobRoleResponse jobRoleResponse = new JobRoleResponse();

        jobRoleResponse.setId(jobRole.getId());

        jobRoleResponse.setJobPosition(jobRole.getJobPosition());

        jobRoleResponse.setJobDescription(jobRole.getJobDescription());

        jobRoleResponse.setDepartmentName(department.getDepartmentName());

        return jobRoleResponse;
    }

    public static LeaveRequest mapToLeaveRequestFromCreateLeaveRequest(CreateLeaveRequest createLeaveRequest, Employee employee, String workEmailAddress, GrantLeaveAuthority grantLeaveAuthority) {

        LeaveRequest leaveRequest = new LeaveRequest();

        leaveRequest.setId(UUID.randomUUID().toString());

        leaveRequest.setNumberOfLeaveDays(createLeaveRequest.getNumberOfLeaveDays());

        leaveRequest.setLeaveType(createLeaveRequest.getLeaveType());

        leaveRequest.setEmployeeId(employee.getId());

        leaveRequest.setEmployeeEmailAddress(workEmailAddress);

        leaveRequest.setGrantLeaveAuthority(grantLeaveAuthority);

        return leaveRequest;
    }

    public static ActiveLeaveResponse mapToActiveLeaveResponseFromEmployeeAndLeaveRequest(LeaveRequest leaveRequest) {

        ActiveLeaveResponse activeLeaveResponse = new ActiveLeaveResponse();

        activeLeaveResponse.setEmployeeId(leaveRequest.getEmployeeId());

        activeLeaveResponse.setEmployeeEmailAddress(leaveRequest.getEmployeeEmailAddress());

        activeLeaveResponse.setNumberOfLeaveDays(leaveRequest.getNumberOfLeaveDays());

        activeLeaveResponse.setLeaveType(leaveRequest.getLeaveType());

        activeLeaveResponse.setGrantLeaveAuthority(leaveRequest.getGrantLeaveAuthority());

        activeLeaveResponse.setApprovedDate(leaveRequest.getApprovedDate());

        activeLeaveResponse.setExpectedReturnDate(leaveRequest.getExpectedReturnDate());

        return activeLeaveResponse;
    }

    public static LeaveResponse mapToLeaveResponseFromLeaveRequest(LeaveRequest leaveRequest) {

        LeaveResponse leaveResponse = new LeaveResponse();

        leaveResponse.setId(leaveRequest.getId());

        leaveResponse.setEmployeeId(leaveRequest.getEmployeeId());

        leaveResponse.setEmployeeEmailAddress(leaveRequest.getEmployeeEmailAddress());

        leaveResponse.setNumberOfLeaveDays(leaveRequest.getNumberOfLeaveDays());

        leaveResponse.setLeaveType(leaveRequest.getLeaveType());

        leaveResponse.setGrantLeaveAuthority(leaveRequest.getGrantLeaveAuthority());

        leaveResponse.setDateCreated(leaveRequest.getDateCreated());

        return leaveResponse;

    }

    public static PayrollRecord mapToPayrollRecordFromPayrollResult(PayrollResult payrollResult, PayrollMessage payrollMessage) {

        PayrollRecord payrollRecord = new PayrollRecord();

        payrollRecord.setId(UUID.randomUUID().toString());

        payrollRecord.setPayrollRunId(payrollMessage.getPayrollRunId());

        payrollRecord.setEmployeeId(payrollResult.getEmployeeId());

        payrollRecord.setPayrollPeriod(payrollResult.getPayrollPeriod().toString());

        payrollRecord.setGrossPay(payrollResult.getGrossPay());

        payrollRecord.setDeductions(payrollResult.getDeductions());

        payrollRecord.setPayrollRecordStatus(PayrollRecordStatus.PROCESSED);

        payrollRecord.setNetPay(payrollResult.getNetPay());

        return payrollRecord;
    }

    public static PayrollRecordResponse mapToPayrollRecordResponseFromPayrollRecord(PayrollRecord payrollRecord, Employee employee) {

        PayrollRecordResponse response = new PayrollRecordResponse();

        response.setEmployeeId(payrollRecord.getEmployeeId());

        response.setPayrollRunId(payrollRecord.getPayrollRunId());

        response.setEmployeeName(employee.getFullName());

        response.setPayrollMonth(payrollRecord.getPayrollPeriod());

        response.setGrossSalary(payrollRecord.getGrossPay());

        response.setDeductions(payrollRecord.getDeductions());

        response.setNetSalary(payrollRecord.getNetPay());

        response.setPayrollRecordStatus(payrollRecord.getPayrollRecordStatus());

        return response;
    }

    public static EmployeePayrollRecordResponse mapToEmployeePayrollRecordResponseFromPayrollRecord(PayrollRecord payrollRecord) {

        EmployeePayrollRecordResponse employeePayrollRecordResponse = new EmployeePayrollRecordResponse();

        employeePayrollRecordResponse.setPayrollMonth(payrollRecord.getPayrollPeriod());

        employeePayrollRecordResponse.setGrossSalary(payrollRecord.getGrossPay());

        employeePayrollRecordResponse.setNetSalary(payrollRecord.getNetPay());

        employeePayrollRecordResponse.setDeductions(payrollRecord.getDeductions());

        employeePayrollRecordResponse.setPayrollRecordStatus(payrollRecord.getPayrollRecordStatus());

        return employeePayrollRecordResponse;

    }

    public static String generateDefaultPassword() {

        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";

        final int LENGTH = 15;

        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder(LENGTH);

        for (int i = 0; i < LENGTH; i++) {

            int index = random.nextInt(CHARACTERS.length());

            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();

    }

    private static final List<String> TRACKED_FIELDS = Arrays.asList(
            "firstname", "middleName", "lastname", "personalEmailAddress",
            "address", "city", "state", "country", "dateOfBirth",
            "gender", "maritalStatus"
    );

    @SuppressWarnings("java:S3011")
    public static double calculateCompletion(Employee employee) {

        long total = TRACKED_FIELDS.size();

        long filled = 0;

        for (String fieldName : TRACKED_FIELDS) {

            try {

                Field field = Employee.class.getDeclaredField(fieldName);

                field.setAccessible(true);

                Object value = field.get(employee);

                if (value != null) {

                    if (value instanceof String) {

                        if (StringUtils.hasText((String) value)) {

                            filled++;
                        }
                    } else {

                        filled++;

                    }
                }
            } catch (Exception exception) {

                log.warn("An error occurred fetching profile completion {}", exception.getMessage());

            }
        }

        return Math.round(((double) filled / total) * 100);

    }

    public static String getLeaveRequestApprovedEmail(String firstName, String resumeDate) {

        return "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                "<h2>Dear " + firstName + ",</h2>" +
                "<p>We are pleased to inform you that your leave request has been <strong>granted</strong>.</p>" +
                "<p>You are expected to <strong>resume fully</strong> on <strong>" + resumeDate + "</strong>.</p>" +
                "<p>Please ensure you are back at your duty station and ready to resume all responsibilities on the specified date.</p>" +
                "<p>If you have any questions or need to discuss your return, feel free to reach out to your supervisor or the HR department.</p>" +
                "<br>" +
                "<p>Thank you for your attention to this matter.</p>" +
                "<p>Best regards,<br>HR Management Team</p>" +
                "</body>" +
                "</html>";
    }

    public static String getOnboardingSuccessEmail(String email, String password) {

        String loginLink = "/login";

        return "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                "<h2>Welcome to Our HR Management System!</h2>" +
                "<p>Congratulations! You have been successfully onboarded into our HR Management System.</p>" +
                "<p>Your account has been created and is ready to use. Below are your login credentials:</p>" +
                "<p><strong>Email:</strong> " + email + "</p>" +
                "<p><strong>Password:</strong> " + password + "</p>" +
                "<p><strong>Important:</strong> Please change your password after your first login for security purposes.</p>" +
                "<p>To get started, please click the link below to login to your dashboard and complete your profile:</p>" +
                "<p><a href='" + loginLink + "'>Login to Dashboard</a></p>" +
                "<p>Make sure to complete your profile information to ensure seamless access to all HR services and benefits.</p>" +
                "<p>If you have any questions or need assistance, please don't hesitate to contact our HR support team.</p>" +
                "<br>" +
                "<p>If you did not expect this email or believe it was sent in error, please contact HR immediately.</p>" +
                "</body>" +
                "</html>";
    }

    public static String getPayslipEmail(String firstName, String month, int year, double amountPaid) {

        return "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                "<h2>Dear " + firstName + ",</h2>" +
                "<p>We are pleased to inform you that your salary for <strong>" + month + " " + year + "</strong> has been processed successfully.</p>" +
                "<p>The total amount paid to your account is <strong>₦" + String.format("%.2f", amountPaid) + "</strong>.</p>" +
                "<p>Please check your wallet balance for the credited amount. If you have any questions or concerns regarding your payslip, feel free to reach out to the HR department.</p>" +
                "<br>" +
                "<p>Thank you for your continued dedication and hard work.</p>" +
                "<p>Best regards,<br>HR Management Team</p>" +
                "</body>" +
                "</html>";
    }
}
