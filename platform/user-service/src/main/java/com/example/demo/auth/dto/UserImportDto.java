package com.example.demo.auth.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.example.demo.auth.converter.UserRoleConverter;
import com.example.demo.auth.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserImportDto {
    @ExcelProperty(value = "学号/工号", index = 0)
    @ColumnWidth(20)
    @NotBlank(message = "学号/工号不能为空")
    private String id;

    @ExcelProperty(value = "用户名", index = 1)
    @ColumnWidth(20)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ExcelProperty(value = "密码", index = 2)
    @ColumnWidth(20)
    @NotBlank(message = "密码不能为空")
    private String password;

    @ExcelProperty(value = "角色", index = 3,converter = UserRoleConverter.class)
    @ColumnWidth(25)
    @NotNull(message = "角色不能为空")
    private UserRole role;

    @ExcelProperty(value = "手机号", index = 4)
    @ColumnWidth(20)
    private String phoneNumber;
}
