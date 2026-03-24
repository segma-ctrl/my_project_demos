package com.example.demo.Controller;
import com.example.demo.Dto.LoginDto;
import com.example.demo.Dto.RegisterDto;
import com.example.demo.Mapper.StudentMapper;
import com.example.demo.Mapper.TeacherMapper;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Tag(name = "用户登录注册接口",description = "根据用户id，用户名，角色进行登录和注册管理")
@RestController
public class RLController {
    @Autowired
    UserMapper userMapper;
    @Autowired
    StudentMapper studentMapper;
    @Autowired
    TeacherMapper teacherMapper;
    //登录
    @Operation(summary = "用户登录",description = "根据用户名，密码，角色进行登录")
    @PostMapping("api/login")
    public Map<String,Object> Login(@RequestBody LoginDto requestData){
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.queryByusername(requestData.getUsername());
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
        if((requestData.getRole()==0&&studentMapper.queryStudentById(requestData.getId())==null)||(requestData.getRole()==1&&teacherMapper.queryTeacherById(requestData.getId())==null)){
            result.put("status",false);
            result.put("message", "此id不存在");
        }
        else{//存在,判断是否已注册，已注册返回注册失败，未注册返回注册成功
            if(userMapper.queryByusername(requestData.getUsername())!= null){
                result.put("status",false);
                result.put("message", "用户已存在");
            }else{
                User user = new User();
                user.setUsername(requestData.getUsername());
                user.setPassword(requestData.getPassword());
                user.setRole(requestData.getRole());
                user.setPhoneNumber(requestData.getPhoneNumber());
                user.setId(requestData.getId());
                user.setCreate_time(new Date());
                if(userMapper.insertuser(user)){
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
