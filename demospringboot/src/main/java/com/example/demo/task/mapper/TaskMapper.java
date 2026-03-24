package com.example.demo.task.mapper;

import com.example.demo.task.entity.Task;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper {
    @Insert("insert into task(TaskId,TaskCount) values(#{TaskId},#{TaskCount})")
    boolean insertTask(Task task);
}
