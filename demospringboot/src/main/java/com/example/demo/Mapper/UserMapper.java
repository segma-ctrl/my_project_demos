package com.example.demo.Mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where username==#{username}")
    public User queryByusername(String username);
    @Insert("")
    public boolean insertuser(User user);
}
