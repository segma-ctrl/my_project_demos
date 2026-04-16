package com.example.demo.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.course.entity.CourseEntity;
import com.example.demo.course.mapper.CourseMapper;
import com.example.demo.course.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, CourseEntity> implements CourseService {

}
