package com.example.demo.reservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.reservation.dto.UserBookingDto;
import com.example.demo.reservation.entity.Booking;
import com.example.demo.reservation.enums.BookStatus;
import com.example.demo.reservation.mapper.BookingMapper;
import com.example.demo.reservation.service.BookingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import com.example.demo.common.exception.ServiceException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {
    @Autowired
    private RedissonClient redissonClient;
    
    @Override
    public boolean createBookingWithLock(Booking booking){
        String lockKey = "lock:resource:" + booking.getResourceId()+":"+booking.getBookingDate();
        RLock lock = redissonClient.getLock(lockKey);
        try{
            if (lock.tryLock(3,10, TimeUnit.SECONDS)){
            throw new ServiceException("系统繁忙，锁定资源失败，请稍后重试");
            }
            //查询一下资源是否被占用
            LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Booking::getResourceId,booking.getResourceId())
                        .eq(Booking::getBookingDate,booking.getBookingDate())
                        .lt(Booking::getStartTime,booking.getEndTime())
                        .gt(Booking::getEndTime,booking.getStartTime())
                        .in(Booking::getStatus,BookStatus.PENDING,BookStatus.APPROVED);
            List<Booking> bookingEntities = this.list(queryWrapper);
            if (bookingEntities.size()>0) {
            throw new ServiceException("该时段资源已被占用，请选择其他时间");
            }
            return this.save(booking);
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new ServiceException("操作被中断");
        }finally{
            if(lock.isLocked()&&lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
    
    
    @Override
    public List<UserBookingDto> getBookingInfoByUserId(String userId){
        //先把对应的Booking对象查出来
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Booking::getUserId,userId);
        List<Booking> bookingEntities = this.list(queryWrapper);

        //然后转换成UserBookingDto对象
        return bookingEntities.stream().map(booking->{
            UserBookingDto dto = new UserBookingDto();
            BeanUtils.copyProperties(booking,dto);
            return dto;
        }).collect(Collectors.toList());
    }
    @Override
    public boolean cancelBookingById(int id){
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus(BookStatus.CANCELED);
        return this.updateById(booking);
    }

    @Override
    public boolean rejectBookingById(int id){
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus(BookStatus.REJECTED);
        return this.updateById(booking);
    }
}
