package com.example.demo.Mapper;

import com.example.demo.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeacherMapper {
    @Select("select * from teacher where id=#{id}")
    public Teacher queryTeacherById(String id);
    @Select("select * from teacher where name= #{name}")
    public Teacher queryTeacherByName(String name);
}
