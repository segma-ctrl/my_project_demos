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
@TableName("booking_cleanup")
public class BookingCleanUpEntity {
    private int id;
    private int bookingId;
    private String userId;
    private String userName;
    private String resourceId;
    private String resourceName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private String applicantPhone;
    private BookStatus status;
    private String approverId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private LocalDateTime delTime;
}
/**
 *     booking_id int comment '预约id',
 *     user_id         varchar(50)  not null comment '用户id',
 *     user_name       varchar(50)  null comment '用户姓名',
 *     resource_id     varchar(50)  not null comment '资源id',
 *     resource_name   varchar(50)  null comment '资源名称',
 *     booking_date    date         not null comment '预约日期',
 *     start_time      time         null comment '预约开始时间',
 *     end_time        time         null comment '预约结束时间',
 *     purpose         varchar(255) null comment '预约用途',
 *     applicant_phone varchar(50)  null comment '联系电话',
 *     status          int          null comment '当前状态（0：待审批，1：已通过，2：被驳回，3：被取消）',
 *     approver_id     varchar(50)  null comment '审批人id',
 *     create_time     datetime     null comment '预约发起时间',
 *     update_time     datetime     null comment '最后更新时间',
 *     del_time        datetime     comment '被清理的时间'
* */