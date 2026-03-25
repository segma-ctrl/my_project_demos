package com.example.demo.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.resource.dto.UserBookingDto;
import com.example.demo.resource.entity.Booking;

import java.util.List;


public interface BookingService extends IService<Booking> {
    List<UserBookingDto> getBookingInfoByUserId(String userId);
}
