package com.example.demo.auth.dto;

import lombok.Data;

@Data
public class ChangePasswordDto {
    private String username;
    private String oldPassword;
    private String newPassword;
}
