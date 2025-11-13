package com.samjay.hr_management_system.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@SuppressWarnings("ALL")
@WritingConverter
public class GenericEnumWriteConverter<T extends Enum<T>> implements Converter<T, String> {

    @Override
    public String convert(T source) {

        return source == null ? null : source.name();

    }
}