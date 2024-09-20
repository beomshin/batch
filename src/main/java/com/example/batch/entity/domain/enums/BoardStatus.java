package com.example.batch.entity.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BoardStatus {

    NORMAL_STATUS(1),
    DELETE_STATUS(2),

    REPORT_STATUS(9)
    ;

    int code;

}
