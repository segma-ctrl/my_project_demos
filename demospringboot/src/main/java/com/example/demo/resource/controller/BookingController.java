package com.example.demo.resource.controller;

import com.example.demo.resource.dto.UserBookingDto;
import com.example.demo.resource.entity.Booking;
import com.example.demo.resource.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    //获取该用户所有预约记录
    @PostMapping("/api/booking/list")
    public List<UserBookingDto> getBookingList(@RequestBody String userId) {
        return bookingService.getBookingInfoByUserId(userId);
    }
    //添加用户预约
    @PostMapping("/api/booking/add")
    public Map<String, Object> addBooking(@RequestBody Booking booking) {
        Map<String, Object> result = new HashMap<>();
        if(bookingService.save(booking)){
            result.put("status",true);
            result.put("message","添加成功");
        }
        else{
            result.put("status",false);
            result.put("message","添加失败");
        }
        return result;
    }
}
