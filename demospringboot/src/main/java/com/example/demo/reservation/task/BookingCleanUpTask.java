package com.example.demo.reservation.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.reservation.entity.Booking;
import com.example.demo.reservation.entity.BookingCleanUpEntity;
import com.example.demo.reservation.enums.BookStatus;
import com.example.demo.reservation.converter.BookingConvert;
import com.example.demo.reservation.service.BookingCleanUpService;
import com.example.demo.reservation.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class BookingCleanUpTask {
    private final BookingService bookingService;
    private final BookingCleanUpService bookingCleanUpService;
    @Autowired
    BookingConvert bookingConvert;

    @Scheduled(cron = "0 0 0 */5 * ?")
    public void cleanUp(){
        //将已完成的预约记录迁移至booking_cleanup表
        LambdaQueryWrapper<Booking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Booking::getStatus, BookStatus.APPROVED,BookStatus.CANCELED,BookStatus.REJECTED)
                .lt(Booking::getEndTime, LocalDateTime.now().minusDays(30))
                .last("limit 1000");
        List<BookingCleanUpEntity> oldbookings= bookingConvert.toBookingCleanUpEntityList(bookingService.list(queryWrapper));
        if(oldbookings.isEmpty()) return ;
        bookingCleanUpService.saveBatch(oldbookings);
        //将迁移的记录从booking表中删除
        List<Integer> ids = oldbookings.stream().map(BookingCleanUpEntity::getBookingId).toList();
        bookingService.removeByIds(ids);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        System.out.println("服务启动完成，开始执行初始化清理任务...");
        this.cleanUp(); 
        System.out.println("初始化清理任务执行完毕！");
    }

}
