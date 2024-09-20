package com.example.batch.entity.domain.converter;

import com.example.batch.entity.domain.enums.PostType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter
public class PostTypeConverter implements AttributeConverter<PostType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PostType postType) {
        if (postType == null) return null;
        return postType.getCode();
    }

    @Override
    public PostType convertToEntityAttribute(Integer s) {
        return Arrays.stream(PostType.values()).filter(x -> x.getCode() == s).findFirst().orElse(null);
    }
}
