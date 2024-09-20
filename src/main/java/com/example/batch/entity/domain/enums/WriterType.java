package com.example.batch.entity.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WriterType {
    ANONYMOUS_TYPE(0),
    MEMBER_TYPE(1),
    LAW_FIRM_TYPE(2)
    ;

    int code;

}
