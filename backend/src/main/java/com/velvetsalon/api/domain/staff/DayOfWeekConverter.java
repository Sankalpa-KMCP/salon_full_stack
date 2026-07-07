package com.velvetsalon.api.domain.staff;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;

@Converter(autoApply = true)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DayOfWeek attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public DayOfWeek convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : DayOfWeek.of(dbData);
    }
}
