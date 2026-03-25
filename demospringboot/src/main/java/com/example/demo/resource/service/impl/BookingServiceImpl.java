package com.example.demo.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.resource.dto.UserBookingDto;
import com.example.demo.resource.entity.Booking;
import com.example.demo.resource.mapper.BookingMapper;
import com.example.demo.resource.service.BookingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {
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

}
