package com.example.batch.entity.domain.converter;

import com.example.batch.entity.domain.enums.BoardStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter
public class BoardStatusConverter implements AttributeConverter<BoardStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(BoardStatus status) {
        return status.getCode();
    }

    @Override
    public BoardStatus convertToEntityAttribute(Integer s) {
        return Arrays.stream(BoardStatus.values()).filter(x -> x.getCode() == s).findFirst().orElse(null);
    }
}
