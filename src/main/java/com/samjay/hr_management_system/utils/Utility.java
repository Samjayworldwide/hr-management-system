package com.samjay.hr_management_system.utils;

import com.samjay.hr_management_system.entities.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class Utility {

    private Utility() {
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
