package com.example.demo.reservation.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.reservation.enums.BookStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("sys_booking")
public class Booking {
    //主键id
    private int id;
    //用户id
    private String userId;
    //用户名
    private String userName;
    //资源id
    private String resourceId;
    //资源名称
    private String resourceName;
    //预约日期
    private LocalDate bookingDate;
    //开始时间
    private LocalTime startTime;
    //结束时间
    private LocalTime endTime;
    //用途
    private String purpose;
    //申请人电话
    private String applicantPhone;
    //状态
    private BookStatus status;
    //审批人id
    private String approverId;
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
