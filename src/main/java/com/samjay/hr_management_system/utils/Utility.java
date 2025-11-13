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
}
