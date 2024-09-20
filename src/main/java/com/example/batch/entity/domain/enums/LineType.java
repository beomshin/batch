package com.example.batch.entity.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LineType {

    TOP(0),
    JUNGLE(1),
    MID(2),
    ADD(3),
    SPT(4),
    ALL(5)
    ;

    int type;

}
