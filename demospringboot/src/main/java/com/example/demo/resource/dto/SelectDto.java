package com.example.demo.resource.dto;

import com.example.demo.resource.enums.ResourceType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
public class SelectDto {
    //资源类型
    private ResourceType type;
    //预约日期
    private LocalDate date;
    //预约开始时间
    private LocalTime startTime;
    //预约结束时间
    private LocalTime endTime;
}
