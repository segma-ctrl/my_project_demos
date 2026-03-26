package com.example.demo.reservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.reservation.dto.UserBookingDto;
import com.example.demo.reservation.entity.Booking;

import java.util.List;


public interface BookingService extends IService<Booking> {
    List<UserBookingDto> getBookingInfoByUserId(String userId);
    boolean cancelBookingById(int id);
}
