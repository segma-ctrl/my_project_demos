package com.example.demo.reservation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.reservation.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

}
