package com.example.demo.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.springframework.stereotype.Component;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(org.apache.ibatis.reflection.MetaObject metaObject) {
        this.setFieldValByName("createTime", java.time.LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateTime", java.time.LocalDateTime.now(), metaObject);
    }
    @Override
    public void updateFill(org.apache.ibatis.reflection.MetaObject metaObject) {
        this.setFieldValByName("updateTime", java.time.LocalDateTime.now(), metaObject);
    }
}
