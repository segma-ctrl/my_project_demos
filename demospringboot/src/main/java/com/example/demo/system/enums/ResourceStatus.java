package com.example.demo.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ResourceStatus {
    AVAILABLE(1,"可用"),
    UNAVAILABLE(0,"不可用");
    @EnumValue
    @JsonValue
    private final int code;
    private final String description;
    ResourceStatus(int code,String description){
        this.code=code;
        this.description=description;
    }

}
