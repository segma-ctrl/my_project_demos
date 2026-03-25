package com.example.demo.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.resource.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

}
