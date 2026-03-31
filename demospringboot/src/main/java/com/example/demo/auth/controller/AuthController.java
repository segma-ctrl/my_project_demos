package com.example.demo.auth.controller;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.auth.dto.ChangePasswordDto;
import com.example.demo.auth.dto.UserImportDto;
import com.example.demo.auth.enums.UserRole;
import com.example.demo.auth.listener.UserImportListener;
import com.example.demo.auth.service.StudentService;
import com.example.demo.auth.service.TeacherService;
import com.example.demo.auth.service.UserService;
import com.example.demo.auth.dto.LoginDto;
import com.example.demo.auth.dto.RegisterDto;
import com.example.demo.auth.entity.User;
import com.example.demo.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Tag(name = "用户登录注册接口",description = "根据用户id，用户名，角色进行登录和注册管理")
@RestController
@RequiredArgsConstructor
@Slf4j
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

    //用户修改密码
    @Operation(summary = "用户修改密码",description = "用户修改密码")
    @PostMapping("api/changePassword")
    public Result changePassword(@RequestBody ChangePasswordDto requestData){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,requestData.getUsername());
        User user = userService.getOne(queryWrapper);
        if(user==null) return Result.error("用户不存在");
        if(!user.getPassword().equals(requestData.getOldPassword())) return Result.error("旧密码错误");
        if(requestData.getOldPassword().equals(requestData.getNewPassword())) return Result.error("新密码不能与旧密码相同");
        if(userService.changePassword(requestData.getUsername(),requestData.getNewPassword())) return Result.success();
        return Result.error("修改密码失败");
    }

    //管理员获取所有用户信息
    @GetMapping("api/admin/user/list")
    public Result<List<User>> getAllUser(){
        return Result.success(userService.list());
    }

    //管理员添加用户（单个）
    @PostMapping("api/admin/user/add")
    public Result addUser(@RequestBody User user){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId,user.getId());
        if(userService.getOne(queryWrapper)!=null){
            return Result.error("此id已存在");
        }
        user.setCreateTime(new Date());
        if(userService.save(user)) return Result.success();
        return Result.error("添加用户失败");
    }

    //管理员下载批量导入用户模板
    @GetMapping("api/admin/user/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        InputStream inputStream = this.getClass()
                .getResourceAsStream("/file/user_import_template.xlsx");

        if (inputStream == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("/file/user_import_template.xlsx");
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=template.xlsx");

        ServletOutputStream outputStream = response.getOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }

        inputStream.close();
        outputStream.flush();
    }
    //批量导入用户
    @PostMapping("api/admin/user/import")
    public Result importUser(@RequestParam("file") MultipartFile file) {
        try{
            InputStream inputStream= file.getInputStream();
            UserImportListener listener = new UserImportListener(userService);
            EasyExcel.read(inputStream, UserImportDto.class, listener).sheet().doRead();
            List<String> errors=listener.saveData();
            if(errors.isEmpty()) return Result.success();
            return Result.error("导入失败："+String.join(",",errors));
        }
        catch (IOException e) {
            return Result.error("文件读取失败");
        }
    }


}
