package com.example.demo.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.reservation.entity.Booking;
import com.example.demo.reservation.enums.BookStatus;
import com.example.demo.reservation.service.BookingService;
import com.example.demo.resource.dto.SelectDto;
import com.example.demo.resource.entity.Resource;
import com.example.demo.resource.enums.ResourceStatus;
import com.example.demo.resource.mapper.ResourceMapper;
import com.example.demo.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {
    @Autowired
    private BookingService bookingService;
    @Override
    public List<Resource> getUsableResource(SelectDto selectDto) {
        //先在资源表中根据资源类型和状态查询出可用的资源
        LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();
        if(selectDto.getType()!=null){//不是勾选的全部类型
            queryWrapper.eq(Resource::getType, selectDto.getType());
        }
        queryWrapper.eq(Resource::getStatus, ResourceStatus.AVAILABLE);
        //再在预约表中根据时间段和状态查询出已被占用的资源
        List<String> resourceIds = bookingService.list(
                new LambdaQueryWrapper<Booking>()
                        .eq(Booking::getBookingDate, selectDto.getDate())
                        .in(Booking::getStatus, BookStatus.PENDING,BookStatus.APPROVED)
                        .lt(Booking::getStartTime, selectDto.getEndTime())
                        .ge(Booking::getEndTime, selectDto.getStartTime())
        ).stream().map(Booking::getResourceId).collect(Collectors.toList());
        //排除这些资源
        if(!resourceIds.isEmpty()){
            queryWrapper.notIn(Resource::getId,resourceIds);
        }
        return this.list(queryWrapper);
    }
}
