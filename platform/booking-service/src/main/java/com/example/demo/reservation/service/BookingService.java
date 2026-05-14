package com.example.demo.reservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.reservation.dto.UserBookingDto;
import com.example.demo.reservation.entity.Booking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingService extends IService<Booking> {
    List<UserBookingDto> getBookingInfoByUserId(String userId);
    boolean cancelBookingById(int id);
    boolean rejectBookingById(int id);
    boolean createBookingWithLock(Booking booking);

    /**
     * 与 {@code [startTime, endTime)} 时段有冲突且为待审/已通过的预约所占用的资源 ID 列表。
     */
    List<String> listConflictingResourceIds(LocalDate date, LocalTime startTime, LocalTime endTime);
}
