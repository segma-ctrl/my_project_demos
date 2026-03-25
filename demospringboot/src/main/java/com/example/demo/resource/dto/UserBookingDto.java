package com.example.demo.resource.dto;

import com.example.demo.resource.enums.BookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "用户预约信息")
public class UserBookingDto {
    @Schema(description = "预约ID")
    private int id;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "资源ID")
    private String resourceId;
    @Schema(description = "资源名")
    private String resourceName;
    @Schema(description = "预约日期")
    private LocalDate bookingDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "当前状态")
    private BookStatus status;

    @Override
    public String toString() {
        return "UserBookingDto{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", bookingDate=" + bookingDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                '}';
    }
}
