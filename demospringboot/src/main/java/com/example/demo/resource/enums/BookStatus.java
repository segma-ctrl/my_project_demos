package com.example.demo.resource.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum BookStatus {
    PENDING(0,"待处理"),
    APPROVED(1,"已通过"),
    REJECTED(2,"被驳回"),
    CANCELED(3,"被取消");

    @EnumValue
    @JsonValue
    private final int code;
    private final String description;
    BookStatus(int code,String description){
        this.code=code;
        this.description=description;
    }
}
