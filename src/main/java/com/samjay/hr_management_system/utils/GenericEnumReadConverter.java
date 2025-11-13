package com.samjay.hr_management_system.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@SuppressWarnings("ALL")
@ReadingConverter
public class GenericEnumReadConverter<T extends Enum<T>> implements Converter<String, T> {

    private final Class<T> enumType;

    public GenericEnumReadConverter(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public T convert(String source) {

        if (source == null) return null;

        return Enum.valueOf(enumType, source);

    }
}
