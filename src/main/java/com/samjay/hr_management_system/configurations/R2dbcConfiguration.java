package com.samjay.hr_management_system.configurations;

import com.samjay.hr_management_system.enumerations.*;
import com.samjay.hr_management_system.utils.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

import java.util.List;

@Configuration
public class R2dbcConfiguration {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {

        List<?> converters = List.of(
                new GenericEnumReadConverter<>(EmploymentStatus.class),
                new GenericEnumWriteConverter<EmploymentStatus>(),
                new GenericEnumReadConverter<>(Gender.class),
                new GenericEnumWriteConverter<Gender>(),
                new GenericEnumReadConverter<>(MaritalStatus.class),
                new GenericEnumWriteConverter<MaritalStatus>(),
                new GenericEnumReadConverter<>(Role.class),
                new GenericEnumWriteConverter<Role>(),
                new GenericEnumReadConverter<>(WorkType.class),
                new GenericEnumWriteConverter<WorkType>(),
                new GenericEnumReadConverter<>(LeaveType.class),
                new GenericEnumWriteConverter<LeaveType>(),
                new GenericEnumReadConverter<>(PayrollRecordStatus.class),
                new GenericEnumWriteConverter<PayrollRecordStatus>(),
                new GenericEnumReadConverter<>(PayrollRunStatus.class),
                new GenericEnumWriteConverter<PayrollRunStatus>()
        );

        return new R2dbcCustomConversions(CustomConversions.StoreConversions.NONE, converters);

    }
}
