package com.example.demo.task;

import com.example.demo.task.mapper.TaskMapper;
import com.example.demo.task.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class demoTask {
    private static int taskcount=0;
    @Autowired
    TaskMapper taskMapper;
    @Scheduled(cron = "0/30 * * * * ?")
    public void task(){
        //update databask
        if(taskMapper.insertTask(new Task(taskcount,++taskcount))){
            System.out.println("定时任务执行第"+taskcount+"次，现在是："+ LocalDateTime.now());
        }
    }
}