package com.example.batch.entity.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PostType {


    NORMAL_TYPE(0),
    IMAGE_TYPE(1),
    RECOMMEND(2),
    BEST_TYPE(3),
    EVENT_TYPE(98),
    NOTICE_TYPE(99),
    ;

    int code;

}
