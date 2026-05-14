package com.example.demo.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserRole {
    STUDENT(0,"学生"),
    TEACHER(1,"教师"),
    MANAGER(2,"管理员");
    @EnumValue
    @JsonValue
    private final int code;
    private final String description;
    UserRole(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
