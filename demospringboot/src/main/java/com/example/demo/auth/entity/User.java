package com.example.demo.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.auth.enums.UserRole;
import lombok.Data;

import java.util.Date;
@Data
@TableName("Auth_user")
public class User {
    private String id;
    private String password;
    private String username;
    private UserRole role;
    private Date createTime;
    private String phoneNumber;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", create_time=" + createTime +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
