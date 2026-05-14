package com.example.demo.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.auth.entity.Teacher;
import com.example.demo.auth.mapper.TeacherMapper;
import com.example.demo.auth.service.TeacherService;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {
}
