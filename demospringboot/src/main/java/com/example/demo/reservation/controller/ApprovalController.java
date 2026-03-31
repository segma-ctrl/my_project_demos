package com.example.demo.reservation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.reservation.entity.Booking;
import com.example.demo.reservation.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.reservation.enums.BookStatus;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApprovalController {
    private final BookingService bookingService;


    //获取审批预约记录
    @GetMapping("/api/approval/list")
    public List<Booking> getApprovalList(@org.springframework.web.bind.annotation.RequestParam(required = false) Integer status){
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            BookStatus reqStatus = null;
            if(status == 0) reqStatus = BookStatus.PENDING;
            else if(status == 1) reqStatus = BookStatus.APPROVED;
            else if(status == 2) reqStatus = BookStatus.REJECTED;
            else if(status == 3) reqStatus = BookStatus.CANCELED;
            queryWrapper.eq(Booking::getStatus, reqStatus);
        }
        queryWrapper.orderByDesc(Booking::getId); // 倒序看最新
        return bookingService.list(queryWrapper);
    }
    //审批预约记录
    @PostMapping("/api/approval/approve")
    public Result approveBooking(@RequestBody int id){
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus(BookStatus.APPROVED);
        if(bookingService.updateById(booking)) return Result.success();
        else return Result.error("操作失败");
    }

    //驳回预约
    @PostMapping("/api/approval/reject")
    public Result rejectBooking(@RequestBody int id){
        if(bookingService.rejectBookingById(id)) return Result.success();
        return Result.error("操作失败");
    }
}
