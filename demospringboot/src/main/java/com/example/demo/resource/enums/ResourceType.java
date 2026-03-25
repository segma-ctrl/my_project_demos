package com.example.demo.resource.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ResourceType {
    CLASSROOM(1,"教室"),
    LAB(2,"实验室"),
    OFFICE(3,"办公室"),
    DIVICE(4,"设备"),
    OTHER(5,"其他");
    @EnumValue
    @JsonValue
    private final int code;
    private final String description;
    ResourceType(int code,String description){
        this.code=code;
        this.description=description;
    }
}
