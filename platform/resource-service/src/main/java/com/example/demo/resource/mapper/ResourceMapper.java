package com.example.demo.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.resource.entity.Resource;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
}
