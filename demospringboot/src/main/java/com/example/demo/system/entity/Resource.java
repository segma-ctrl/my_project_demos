package com.example.demo.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.system.enums.ResourceStatus;
import com.example.demo.system.enums.ResourceType;
import lombok.Data;

@Data
@TableName("Sys_resource")
public class Resource {
    private String id;
    private String name;
    private ResourceType type;
    private String location;//暂定，我感觉这个应该也是一个枚举类型
    private int capacity;
    private ResourceStatus status;
    private String description;
}
