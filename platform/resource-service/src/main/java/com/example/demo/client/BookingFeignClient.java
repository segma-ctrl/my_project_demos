package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "booking-service", url = "${booking.service.url:http://127.0.0.1:9003}")
public interface BookingFeignClient {

    @GetMapping("/internal/bookings/conflicting-resource-ids")
    List<String> conflictingResourceIds(
            @RequestParam("date") String date,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime
    );
}
