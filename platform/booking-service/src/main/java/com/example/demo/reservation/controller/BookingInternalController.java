package com.example.demo.reservation.controller;

import com.example.demo.reservation.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 供其它微服务（如 resource-service）通过 Feign 调用的内部接口。
 */
@RestController
@RequestMapping("/internal/bookings")
@RequiredArgsConstructor
public class BookingInternalController {

    private final BookingService bookingService;

    @GetMapping("/conflicting-resource-ids")
    public List<String> conflictingResourceIds(
            @RequestParam("date") String date,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime
    ) {
        return bookingService.listConflictingResourceIds(
                LocalDate.parse(date),
                LocalTime.parse(startTime),
                LocalTime.parse(endTime)
        );
    }
}
