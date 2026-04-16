package com.example.demo.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("sys_course")
public class CourseEntity {
    private int id;
    private String name;
    private LocalDate  startTime;
    private LocalDate  endTime;
    private String teacherId;
}
