package com.example.demo.reservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.reservation.dto.UserBookingDto;
import com.example.demo.reservation.entity.Booking;
import com.example.demo.reservation.enums.BookStatus;
import com.example.demo.reservation.mapper.BookingMapper;
import com.example.demo.reservation.service.BookingService;
import com.mybs.common.ServiceException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean createBookingWithLock(Booking booking) {
        String lockKey = "lock:resource:" + booking.getResourceId() + ":" + booking.getBookingDate();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new ServiceException("系统繁忙，锁定资源失败，请稍后重试");
            }
            LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Booking::getResourceId, booking.getResourceId())
                    .eq(Booking::getBookingDate, booking.getBookingDate())
                    .lt(Booking::getStartTime, booking.getEndTime())
                    .gt(Booking::getEndTime, booking.getStartTime())
                    .in(Booking::getStatus, BookStatus.PENDING, BookStatus.APPROVED);
            List<Booking> bookingEntities = this.list(queryWrapper);
            if (!bookingEntities.isEmpty()) {
                throw new ServiceException("该时段资源已被占用，请选择其他时间");
            }
            return this.save(booking);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("操作被中断");
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<String> listConflictingResourceIds(LocalDate date, LocalTime startTime, LocalTime endTime) {
        return this.list(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getBookingDate, date)
                .in(Booking::getStatus, BookStatus.PENDING, BookStatus.APPROVED)
                .lt(Booking::getStartTime, endTime)
                .gt(Booking::getEndTime, startTime)
        ).stream().map(Booking::getResourceId).collect(Collectors.toList());
    }

    @Override
    public List<UserBookingDto> getBookingInfoByUserId(String userId) {
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getUserId, userId);
        List<Booking> bookingEntities = this.list(queryWrapper);

        return bookingEntities.stream().map(booking -> {
            UserBookingDto dto = new UserBookingDto();
            BeanUtils.copyProperties(booking, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean cancelBookingById(int id) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus(BookStatus.CANCELED);
        return this.updateById(booking);
    }

    @Override
    public boolean rejectBookingById(int id) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus(BookStatus.REJECTED);
        return this.updateById(booking);
    }
}
