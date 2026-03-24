package com.example.demo.auth.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.auth.enums.UserRole;
import com.example.demo.auth.service.StudentService;
import com.example.demo.auth.service.TeacherService;
import com.example.demo.auth.service.UserService;
import com.example.demo.common.dto.LoginDto;
import com.example.demo.common.dto.RegisterDto;
import com.example.demo.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Tag(name = "用户登录注册接口",description = "根据用户id，用户名，角色进行登录和注册管理")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    //登录
    @Operation(summary = "用户登录",description = "根据用户名，密码，角色进行登录")
    @PostMapping("api/login")
    public Map<String,Object> Login(@RequestBody LoginDto requestData){
        Map<String, Object> result = new HashMap<>();
        User user=userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, requestData.getUsername())
        );
        if(user==null){
            result.put("status", false);
            result.put("message", "用户不存在");
        }else if(!requestData.getPassword().equals(user.getPassword())){
            result.put("status",false);
            result.put("message", "密码错误");
        }else if(user.getRole()!=requestData.getRole()){
            result.put("status",false);
            result.put("message", "权限错误");
        }
        else{
            result.put("status",true);
            result.put("message", "登录成功");
        }
        return result;
    }

    //注册
    @Operation(summary = "用户注册",description = "新用户注册")
    @PostMapping("api/register")
    public Map<String, Object> Register(@RequestBody RegisterDto requestData) {
        Map<String, Object> result = new HashMap<>();
        //是否存在
        if((requestData.getRole()==UserRole.STUDENT&&studentService.getById(requestData.getId())==null)
                ||(requestData.getRole()==UserRole.TEACHER&&teacherService.getById(requestData.getId())==null)){
            result.put("status",false);
            result.put("message", "此id不存在");
        }
        else{
            //存在,判断是否已注册，已注册返回注册失败，未注册返回注册成功
            if(userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, requestData.getUsername()))!= null){
                result.put("status",false);
                result.put("message", "用户已存在");
            }else{
                User user = new User();
                user.setUsername(requestData.getUsername());
                user.setPassword(requestData.getPassword());
                user.setRole(requestData.getRole());
                user.setPhoneNumber(requestData.getPhoneNumber());
                user.setId(requestData.getId());
                user.setCreateTime(new Date());
                if(userService.save(user)){
                    //插入数据成功
                    result.put("status",true);
                    result.put("message", "注册成功");
                }else {
                    result.put("status",false);
                    result.put("message", "系统错误,注册失败");
                }
            }
        }
        return result;
    }

}
