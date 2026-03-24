package com.example.demo.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class demoTask {
    @Scheduled(cron = "0/30 * * * * ?")
    public void task(){
        //update database
        System.out.println("定时任务执行，现在是："+ LocalDateTime.now());
    }
}
