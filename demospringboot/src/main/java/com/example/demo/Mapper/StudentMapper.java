package com.example.demo.Mapper;

import com.example.demo.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentMapper {
    @Select("select * from student where id==#{id}")
    public Student queryStudentById(String id);
    @Select("select * from student where name==#{name}")
    public Student queryStudentByName(String name);
}
