package com.example.demo.reservation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.reservation.entity.BookingCleanUpEntity;
import com.example.demo.reservation.mapper.BookingCleanUpMapper;
import com.example.demo.reservation.service.BookingCleanUpService;
import org.springframework.stereotype.Service;

@Service
public class BookingCleanUpServiceImpl extends ServiceImpl<BookingCleanUpMapper, BookingCleanUpEntity> implements BookingCleanUpService {
}
